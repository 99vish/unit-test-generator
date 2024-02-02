package com.blumeglobal.tests.controller;

import com.blumeglobal.tests.main.TestGenerator;
import com.blumeglobal.tests.model.request.PathRequest;
import lombok.Getter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api")
public class PathController {

    @Getter
    private static Path projectPath;
    @Getter
    private static Path excelPath;

    @PostMapping("/updatePaths")
    public ResponseEntity<String> updatePaths(@RequestBody PathRequest pathRequest) {

        projectPath = pathRequest.getProjectPath();
        excelPath = pathRequest.getExcelPath();
        TestGenerator.generateTests();

        return ResponseEntity.ok("Paths updated successfully!");
    }





}



