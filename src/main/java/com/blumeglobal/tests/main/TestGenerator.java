package com.blumeglobal.tests.main;

import com.blumeglobal.tests.builder.ClassDeclarationBuilderImpl;
import com.blumeglobal.tests.builder.MethodDeclarationBuilderImpl;
import com.blumeglobal.tests.builder.interfaces.ClassDeclarationBuilder;
import com.blumeglobal.tests.builder.interfaces.MethodDeclarationBuilder;
import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.controller.PathController;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.excel.MethodParameter;
import com.blumeglobal.tests.model.output.TestClassDeclaration;
import com.blumeglobal.tests.util.FileReaderUtil;
import com.blumeglobal.tests.util.PathGeneratorUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestGenerator {


    public static void generateTests(String className, List<InputTestCases> inputTestCasesList){

        Path excelPath = PathController.getCompletedExcelPath();
        Workbook workbook = null;

        try {
            workbook = new XSSFWorkbook(Files.newInputStream(excelPath));
        } catch (
        IOException e) {
            throw new RuntimeException(e);
        }

        //List<InputTestCases> inputTestCasesList = InputTestCasesCache.getInputTestCasesList();
        List<MethodParameter> methodParametersList=FileReaderUtil.readExcelFile(excelPath.toString(), workbook.getSheet("MethodParams"),MethodParameter.class);

        MethodDeclarationBuilder methodDeclarationBuilder=new MethodDeclarationBuilderImpl(methodParametersList);
        ClassDeclarationBuilder classDeclarationBuilder=new ClassDeclarationBuilderImpl(methodDeclarationBuilder,inputTestCasesList,className);
        TestClassDeclaration classDeclaration = classDeclarationBuilder.buildClassDeclarations();


        Map<String,Object> DataModel = new HashMap<>();
        DataModel.put("packageName",classDeclaration.getPackageName());
        DataModel.put("importsList",classDeclaration.getImports());
        DataModel.put("className",className);
        DataModel.put("classInstance",Character.toLowerCase(className.charAt(0)) + className.substring(1));
        DataModel.put("methodsList",classDeclaration.getMethodDeclarations());
        DataModel.put("fields",classDeclaration.getDependentFieldClasses());
        DataModel.put("isConstructorPresent",classDeclaration.getIsConstructorPresent());


        processDataWithTemplate(null,"src\\main\\resources\\templates\\AbstractIntegrationTestTemplate.ftl", PathGeneratorUtil.getPathForUtilCLassGeneration(classDeclaration.getClassPath(),"AbstractIntegrationTest.java"));
        processDataWithTemplate(null,"src\\main\\resources\\templates\\JsonFileReaderUtil.ftl",PathGeneratorUtil.getPathForUtilCLassGeneration(classDeclaration.getClassPath(),"JsonFileReaderUtil.java"));

        processDataWithTemplate(DataModel, "src\\main\\resources\\templates\\ControllerTemplate.ftl", PathGeneratorUtil.getTestFolderPath(classDeclaration.getClassPath(),className));

        System.out.println("Test cases have been generated for "+className);



    }
    private static void processDataWithTemplate(Map<String,Object>DataModel, String pathToTemplate, Path path){

        FileReader templateReader = readTemplate(pathToTemplate);
        Configuration config = new Configuration();
        Template testMethodTemplate=null;
        try {
            testMethodTemplate=new Template("testMethodTemplate",templateReader,config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringWriter output = new StringWriter();
        try {
            testMethodTemplate.process(DataModel, output);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileWriter fileWriter = writeTestMethods(path.toString());
        try {
            fileWriter.write(output.toString());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static FileWriter writeTestMethods(String filepath) {

        FileWriter fileWriter=null;
        try {
            fileWriter=new FileWriter(filepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileWriter;
    }
    public static FileReader readTemplate(String pathToTemplate)
    {
        FileReader templateReader=null;
        try {
            templateReader=new FileReader(pathToTemplate);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return templateReader;
    }

}
