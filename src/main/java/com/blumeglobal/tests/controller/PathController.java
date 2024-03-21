package com.blumeglobal.tests.controller;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.main.ExcelTemplateGenerator;
import com.blumeglobal.tests.main.TestGenerator;
import com.blumeglobal.tests.model.excel.ExcelTemplate;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.request.ExcelPathRequest;
import com.blumeglobal.tests.model.request.InputTestCasesRequest;
import com.blumeglobal.tests.model.request.PathRequest;
import com.blumeglobal.tests.util.JavaParserUtil;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PathController {

    @Getter
    private Path projectPath;
    @Getter
    private Path excelPath;
    @Getter
    private static Path completedExcelPath;

    @Getter
    public  List<InputTestCases> inputTestCasesList = new ArrayList<>();
    @Getter
    public  List<String> inputMethodNamesByClassName = new ArrayList<>();
    @Getter
    public  Map<String, List<InputTestCases>> inputTestCasesByClassNameMap = new HashMap<>();
    @Getter
    public  List<String> inputClassNames = new ArrayList<>();


    @PostMapping("/getControllersAndMethods")
    public ResponseEntity<List<Map<String,List<String>>>> getControllersAndMethods(@RequestBody PathRequest projectPathRequest) {

        Cache.cacheClassOrInterfaceDeclarations(projectPathRequest.getProjectPath().toString());
        List<ClassOrInterfaceDeclaration> controllerClasses = JavaParserUtil.getClassOrInterfaceDeclarationByType(Cache.getClassOrInterfaceDeclarations(),"RestController");
        controllerClasses.addAll(JavaParserUtil.getClassOrInterfaceDeclarationByType(Cache.getClassOrInterfaceDeclarations(),"Controller"));
        List<Map<String,List<String>>> ControllerClassesAndMethodList = new ArrayList<>();
        for(ClassOrInterfaceDeclaration classOrInterfaceDeclaration:controllerClasses){
            List<String> methods = Cache.getMethodDeclarationsByClassName(classOrInterfaceDeclaration.getNameAsString());
            Map<String,List<String>>map = new HashMap<>();
            map.put(classOrInterfaceDeclaration.getNameAsString(), methods);
            ControllerClassesAndMethodList.add(map);
        }
        return ResponseEntity.ok().body(ControllerClassesAndMethodList);
    }

    @PostMapping("/generateTemplatesFromSelection")
    public ResponseEntity<List<ExcelTemplate>> generateTemplates(@RequestBody InputTestCasesRequest inputTestCasesRequest){

        inputTestCasesList.clear();
        inputMethodNamesByClassName.clear();
        inputTestCasesByClassNameMap.clear();
        inputClassNames.clear();

        inputTestCasesList=inputTestCasesRequest.getInputTestCasesList();
//        for(InputTestCasesRequest inputTestCasesRequest : inputTestCasesRequests){
//            InputTestCases inputTestCase = new InputTestCases();
//            inputTestCase.setClassName(inputTestCasesRequest.getClassName());
//            inputTestCase.setMethodName(inputTestCasesRequest.getMethodName());
//            inputTestCase.setAssertionParameters(inputTestCasesRequest.getAssertionParameter());
//            inputTestCasesList.add(inputTestCase);
//        }

        InputTestCasesCache inputTestCasesCache = new InputTestCasesCache();
        inputTestCasesCache.populateInputTestCasesByClassNameMap(inputTestCasesList,inputTestCasesByClassNameMap,inputClassNames);

        List <ExcelTemplate> templates = new ArrayList<>();

        for(String className:inputClassNames){
            templates.add(ExcelTemplateGenerator.generateExcelTemplate(className,inputTestCasesByClassNameMap.get(className)));
        }


        return ResponseEntity.ok().body(templates);
    }

    @PostMapping("/updatePaths")
    public ResponseEntity<List<ExcelTemplate>> updatePaths(@RequestBody PathRequest pathRequest) {

        inputTestCasesList.clear();
        inputMethodNamesByClassName.clear();
        inputTestCasesByClassNameMap.clear();
        inputClassNames.clear();

        projectPath = pathRequest.getProjectPath();
        excelPath = pathRequest.getExcelPath();

        Cache.cacheClassOrInterfaceDeclarations(projectPath.toString());//883

        InputTestCasesCache inputTestCasesCache =  new InputTestCasesCache();
        inputTestCasesCache.cacheInputTestCases(excelPath,inputTestCasesList,inputTestCasesByClassNameMap,inputMethodNamesByClassName,inputClassNames);

        List <ExcelTemplate> templates = new ArrayList<>();

        for(String className:inputClassNames){
            templates.add(ExcelTemplateGenerator.generateExcelTemplate(className,inputTestCasesByClassNameMap.get(className)));
        }


        return ResponseEntity.ok().body(templates);
    }


    @PostMapping("/generateTests")
    public ResponseEntity<String> generateTests(@RequestBody ExcelPathRequest excelPathRequest) {

        completedExcelPath = excelPathRequest.getCompletedExcelPath();
        String className = excelPathRequest.getClassName();
        System.out.println("ye khaali hai" + inputTestCasesByClassNameMap.size());
        TestGenerator.generateTests(className,inputTestCasesByClassNameMap.get(className));

        return ResponseEntity.ok("Tests Generated Successfully");
    }


}