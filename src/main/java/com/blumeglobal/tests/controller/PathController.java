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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
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
    public  static List<String> inputMethodNamesByClassName = new ArrayList<>();
    @Getter
    public  static Map<String, List<InputTestCases>> inputTestCasesByClassNameMap = new HashMap<>();
    @Getter
    public  List<String> inputClassNames = new ArrayList<>();
    @Getter
    public static Map<String, Set<String>> inputTestCasesByClassNameMapSet = new HashMap<>();


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
    public ResponseEntity<List<ExcelTemplate>> generateTemplates(@RequestBody InputTestCasesRequest inputTestCasesRequest) throws JsonProcessingException {

        inputTestCasesList.clear();
        inputMethodNamesByClassName.clear();
        inputTestCasesByClassNameMap.clear();
        inputClassNames.clear();

        inputTestCasesList=inputTestCasesRequest.getInputTestCasesList();

        InputTestCasesCache inputTestCasesCache = new InputTestCasesCache();
        inputTestCasesCache.populateInputTestCasesByClassNameMap(inputTestCasesList,inputTestCasesByClassNameMap,inputClassNames);

        List <ExcelTemplate> templates = new ArrayList<>();

        for(String className:inputClassNames){
            templates.add(ExcelTemplateGenerator.generateExcelTemplate(className,inputTestCasesByClassNameMap.get(className)));
        }


        return ResponseEntity.ok().body(templates);
    }

    @PostMapping("/updatePaths")
    public ResponseEntity<String> updatePaths(@RequestBody PathRequest pathRequest) throws JsonProcessingException {

        inputTestCasesList.clear();
        inputMethodNamesByClassName.clear();
        inputTestCasesByClassNameMap.clear();
        inputClassNames.clear();

        projectPath = pathRequest.getProjectPath();
        completedExcelPath = pathRequest.getExcelPath();

        Cache.cacheClassOrInterfaceDeclarations(projectPath.toString());
//
//        InputTestCasesCache inputTestCasesCache =  new InputTestCasesCache();
//        inputTestCasesCache.cacheInputTestCases(excelPath,inputTestCasesList,inputTestCasesByClassNameMap,inputMethodNamesByClassName,inputClassNames);

        inputTestCasesList=getInputTestCasesFromExcel(completedExcelPath);

        InputTestCasesCache inputTestCasesCache = new InputTestCasesCache();
        inputTestCasesCache.populateInputTestCasesByClassNameMap(inputTestCasesList,inputTestCasesByClassNameMap,inputClassNames);

//        List <ExcelTemplate> templates = new ArrayList<>();
//
//        for(String className:inputClassNames){
//            templates.add(ExcelTemplateGenerator.generateExcelTemplate(className,inputTestCasesByClassNameMap.get(className)));
//        }

        populateInputTestCasesByClassNameMapSet(completedExcelPath,inputTestCasesByClassNameMapSet);
        inputTestCasesByClassNameMapSet.forEach((className, inputTestCasesSet) -> {
            try {
                TestGenerator.generateTests(className, inputTestCasesSet);
            } catch (IOException e) {
                // Handle the exception
                e.printStackTrace();
            }
        });


        return ResponseEntity.ok().body("Tests Generated Successfully");
    }


    @PostMapping("/generateTests")
    public ResponseEntity<String> generateTests(@RequestBody ExcelPathRequest excelPathRequest) {

        completedExcelPath = excelPathRequest.getCompletedExcelPath();
        populateInputTestCasesByClassNameMapSet(completedExcelPath,inputTestCasesByClassNameMapSet);
        inputTestCasesByClassNameMapSet.forEach((className, inputTestCasesSet) -> {
            try {
                TestGenerator.generateTests(className, inputTestCasesSet);
            } catch (IOException e) {
                // Handle the exception
                e.printStackTrace();
            }
        });

        return ResponseEntity.ok("Tests Generated Successfully");
    }

    private void populateInputTestCasesByClassNameMapSet(Path excelPath ,Map<String,Set<String>> inputTestCasesByClassNameMapSet){
        try (FileInputStream fis = new FileInputStream(excelPath.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("MethodDetails");
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet 'MethodDetails' does not exist in the provided Excel file.");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String className = row.getCell(0).getStringCellValue();
                    String methodName = row.getCell(1).getStringCellValue();

                    inputTestCasesByClassNameMapSet
                            .computeIfAbsent(className, k -> new HashSet<>())
                            .add(methodName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<InputTestCases> getInputTestCasesFromExcel(Path excelPath){
        List<InputTestCases> testCasesList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelPath.toFile()) ;
            Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet("MethodDetails"); // Accessing the sheet by name
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet 'methodDetails' not found in the workbook");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String className = row.getCell(0).getStringCellValue();
                    String methodName = row.getCell(1).getStringCellValue();
                    InputTestCases inputTestCase = new InputTestCases();
                    inputTestCase.setClassName(className);
                    inputTestCase.setMethodName(methodName);
                    testCasesList.add(inputTestCase);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testCasesList;
    }

}


