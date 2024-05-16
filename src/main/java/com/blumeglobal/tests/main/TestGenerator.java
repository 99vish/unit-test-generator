package com.blumeglobal.tests.main;

import com.blumeglobal.tests.builder.ClassDeclarationBuilderImpl;
import com.blumeglobal.tests.builder.MethodDeclarationBuilderImpl;
import com.blumeglobal.tests.builder.interfaces.ClassDeclarationBuilder;
import com.blumeglobal.tests.builder.interfaces.MethodDeclarationBuilder;
import com.blumeglobal.tests.controller.PathController;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.excel.MethodParameter;
import com.blumeglobal.tests.model.output.TestClassDeclaration;
import com.blumeglobal.tests.util.ExcelUtil;
import com.blumeglobal.tests.util.FileReaderUtil;
import com.blumeglobal.tests.util.PathGeneratorUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestGenerator {


    public static void generateTests(String className, List<InputTestCases> inputTestCasesList) throws IOException {

        Path excelPath = PathController.getCompletedExcelPath();

        Workbook workbook = null;

        try {
            workbook = new XSSFWorkbook(Files.newInputStream(excelPath));
        } catch (
        IOException e) {
            throw new RuntimeException(e);
        }

        Map<String,List<List<MethodParameter>>> methodNameToParametersMap = getMethodNameToParametersMap(workbook,className);

        MethodDeclarationBuilder methodDeclarationBuilder=new MethodDeclarationBuilderImpl(methodNameToParametersMap);
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
            processDataWithTemplate(null,"src\\main\\resources\\templates\\ValidationUtil.ftl",PathGeneratorUtil.getPathForUtilCLassGeneration(classDeclaration.getClassPath(),"ValidationUtil.java"));

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

    private static Map<String,List<List<MethodParameter>>> getMethodNameToParametersMap(Workbook workbook, String className) throws JsonProcessingException {
        List<InputTestCases > inputTestCasesList = PathController.getInputTestCasesByClassNameMap().get(className);
        List<String>methodNames = new ArrayList<>();
        for(InputTestCases inputTestCases: inputTestCasesList){
            if(!methodNames.contains(inputTestCases.getMethodName())){
                methodNames.add(inputTestCases.getMethodName());
            }

        }
        return populateMethodNameToParametersMap(workbook,className,methodNames);
    }

    private static Map<String,List<List<MethodParameter>>> populateMethodNameToParametersMap(Workbook workbook,String className, List<String>methodNames) throws JsonProcessingException {
        Sheet sheet = workbook.getSheet("MethodDetails");
        Map<String,List<List<MethodParameter>>> requiredMap = new HashMap<>();
        for(String methodName:methodNames) {
            List<List<MethodParameter>> methodParamsRows = new ArrayList<>();
            int[] filteredRowNum = ExcelUtil.getFilteredRows(sheet, className, methodName);
            int startingIndex = filteredRowNum[0];
            int endingIndex = filteredRowNum[1];
            for (int i = startingIndex + 1; i <= endingIndex; i++) {
                Row headerRow = sheet.getRow(startingIndex);
                Row currentRow = sheet.getRow(i);
                List<MethodParameter> methodParams = new ArrayList<>();
                for (int k = 2; k < headerRow.getLastCellNum(); k++) {

                    if(currentRow.getCell(k) != null && !currentRow.getCell(k).getStringCellValue().isEmpty()){
                        if(headerRow.getCell(k).getStringCellValue().equals("Method Parameters")){
                            ObjectMapper mapper = new ObjectMapper();
                            String jsonContent = currentRow.getCell(k).getStringCellValue();
                            Map<String, String> jsonMap = mapper.readValue(jsonContent, new TypeReference<Map<String, String>>() {});

                            for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
                                String paramName = entry.getKey();
                                String paramValue = entry.getValue();
                                MethodParameter param = new MethodParameter();
                                param.setClassName(className);
                                param.setMethodName(methodName);
                                param.setParameterName(paramName);
                                param.setParameterValue(paramValue);
                                methodParams.add(param);
                            }
                        } else {
                            MethodParameter param = new MethodParameter();
                            param.setClassName(className);
                            param.setMethodName(methodName);
                            param.setParameterName(headerRow.getCell(k).getStringCellValue());
                            param.setParameterValue(currentRow.getCell(k).getStringCellValue());
                            methodParams.add(param);
                        }
                    }
                }
                methodParamsRows.add(methodParams);
            }
            requiredMap.put(methodName, methodParamsRows);
        }
        return requiredMap;

    }



}
