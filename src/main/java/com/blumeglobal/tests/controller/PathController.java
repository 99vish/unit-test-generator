package com.blumeglobal.tests.controller;

import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.main.CacheCreater;
import com.blumeglobal.tests.main.ExcelTemplateGenerator;
import com.blumeglobal.tests.main.TestGenerator;
import com.blumeglobal.tests.model.excel.ExcelTemplate;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.request.ExcelPathRequest;
import com.blumeglobal.tests.model.request.PathRequest;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PathController {

    @Getter
    private static Path projectPath;
    @Getter
    private static Path excelPath;
    @Getter
    private static Path interfacesPath;
    @Getter
    private static Path completedExcelPath;

    @PostMapping("/updatePaths")
    public ResponseEntity<List<ExcelTemplate>> updatePaths(@RequestBody PathRequest pathRequest) {

        projectPath = pathRequest.getProjectPath();
        excelPath = pathRequest.getExcelPath();
        interfacesPath = pathRequest.getInterfacesPath();
        CacheCreater.createCache();

        List <ExcelTemplate> templates = new ArrayList<>();

        for(String className:InputTestCasesCache.getInputClassNames()){
            templates.add(ExcelTemplateGenerator.generateExcelTemplate(className));
        }


        return ResponseEntity.ok().body(templates);
    }


    @PostMapping("/generateTests")
    public ResponseEntity<String> generateTests(@RequestBody ExcelPathRequest excelPathRequest) {

        completedExcelPath = excelPathRequest.getCompletedExcelPath();
        String className = excelPathRequest.getClassname();
        TestGenerator.generateTests(className);

        return ResponseEntity.ok("Tests Generated Successfully");
    }


}