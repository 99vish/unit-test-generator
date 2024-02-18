package com.blumeglobal.tests.builder;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.controller.PathController;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.output.Argument;
import com.blumeglobal.tests.model.output.MethodDeclaration;
import com.blumeglobal.tests.model.excel.MethodParameter;
import com.blumeglobal.tests.builder.interfaces.MethodDeclarationBuilder;
import com.blumeglobal.tests.util.ExcelToJsonDataGeneratorUtil;
import com.blumeglobal.tests.util.PathGeneratorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MethodDeclarationBuilderImpl implements MethodDeclarationBuilder {

    public List<MethodParameter> inputMethodParams;

    public MethodDeclarationBuilderImpl(List<MethodParameter> inputMethodParams) {
        this.inputMethodParams = inputMethodParams;
    }

    @Override
    public List<MethodDeclaration> buildMethodDeclarations(String className) {

        Path classPath = Cache.getPathFromClassDeclaration(Cache.getClassOrInterfaceDeclarationByClassName(className));

        List<MethodDeclaration> methodDeclarations=new ArrayList<>();

        List<String> methodNamesByClassNameFromExcel = getGivenMethodNamesByClassName(className);

        for (String methodName : methodNamesByClassNameFromExcel) {

            com.github.javaparser.ast.body.MethodDeclaration javaParserMethodDeclaration = Cache.getMethodDeclaration(className, methodName);

            //String entity = javaParserMethodDeclaration.getType().asClassOrInterfaceType().getChildNodes().get(1).getChildNodes().get(1).toString();

            Path excelForJsonPath = getRequestSheetPath(PathController.getCompletedExcelPath().toString(),className,methodName);

            MethodDeclaration methodDeclaration=new MethodDeclaration();
            //methodDeclaration.setEntityType(entity);
            methodDeclaration.setMethodName(methodName);

            String requestProperties = InputTestCasesCache.getRequestPropertyStringByClassNameAndMethodName(className,methodName);
            List<String> requestPropertiesList = getRequestPropertiesAsList(requestProperties);

            methodDeclaration.setRequestProperties(requestPropertiesList);

            methodDeclaration.setReturnValue(javaParserMethodDeclaration.getType().asClassOrInterfaceType().getTypeArguments().get().get(0).asString());

            NodeList<Parameter> parameterNodeList = javaParserMethodDeclaration.getParameters();
            List<Parameter> parameterList = new ArrayList<>(parameterNodeList);

            if(!parameterList.isEmpty()) {
                for (Parameter parameter : parameterList) {

                    Argument argument = new Argument();
                    String parameterName = parameter.getName().getIdentifier();
                    argument.setName(parameterName);
                    ClassOrInterfaceType type = (ClassOrInterfaceType) parameter.getType();
                    argument.setDataType(type.getName().getIdentifier());

                    if(parameter.getAnnotations().get(0).getNameAsString().equals("RequestBody")) {

                        argument.setAnnotationType("RequestBody");
                        methodDeclaration.setHasRequestBody(true);
                        //if there is a request body present then the entity inside the requestbody should be used to take the entity type
                        methodDeclaration.setEntityType(parameter.getType().asClassOrInterfaceType().getChildNodes().get(1).toString());

                    }

                    if(parameter.getAnnotations().get(0).getNameAsString().equals("RequestParam"))
                    {
                        methodDeclaration.setHasRequestParam(true);
                        argument.setAnnotationType("RequestParam");
                    }

                    if(parameter.getAnnotations().get(0).getNameAsString().equals("PathVariable"))
                    {
                        methodDeclaration.setHasPathVariable(true);
                        argument.setAnnotationType("PathVariable");
                    }

                    String parameterValue = getMethodParameter(className, javaParserMethodDeclaration.getNameAsString(), parameter.getNameAsString());

                    //if apirequest is not present then,the entity type will be set by responsetype
                    if(methodDeclaration.getEntityType() == null){
                        methodDeclaration.setEntityType(javaParserMethodDeclaration.getType().asClassOrInterfaceType().getChildNodes().get(1).getChildNodes().get(1).toString());
                    }

                    methodDeclaration.setReturnEntityType(javaParserMethodDeclaration.getType().asClassOrInterfaceType().getChildNodes().get(1).getChildNodes().get(1).toString());


                    if (argument.getName().equals("apiRequest")) {

                        argument.setValue("apijson");

                        Path pathToJsonFile = PathGeneratorUtil.getPathForJsonRequestGeneration(classPath,className,methodName);
                        argument.setPathToJsonFile(pathToJsonFile.toString().replace("\\","\\\\"));
                        String value= ExcelToJsonDataGeneratorUtil.generateJsonString(excelForJsonPath,className,methodName);

                        generateAndWriteJson(pathToJsonFile.toString(),value);
                    }
                    else {
                        argument.setValue(parameterValue);
                    }
                    methodDeclaration.getArguments().add(argument);
                }
            }
            methodDeclarations.add(methodDeclaration);
        }
        return methodDeclarations;
    }

    private String getMethodParameter(String className, String methodName, String parameterName) {
        for (MethodParameter parameter : inputMethodParams) {
            if (parameter.getClassName().equalsIgnoreCase(className) && parameter.getMethodName().equalsIgnoreCase(methodName) && parameter.getParameterName().equalsIgnoreCase(parameterName)) {
                return parameter.getParameterValue();
            }
        }
        return null;
    }

    private List<String> getRequestPropertiesAsList(String inputString){
        String[] partsArray = inputString.split(",");
        List<String> resultList = new ArrayList<>();

        for (String part : partsArray) {
            String trimmedPart = part.trim();
            resultList.add(trimmedPart);
        }

        return resultList;
    }

    private List<String> getMethodNamesByClassName (String className){
        List<InputTestCases> inputTestCases = InputTestCasesCache.getInputTestCasesList();
        List<String> methodsList = new ArrayList<>();
        for(InputTestCases inputTestCase: inputTestCases){
            if(inputTestCase.getClassName().equals(className)){
                methodsList.add(inputTestCase.getMethodName());
            }
        }
        return methodsList;
    }

    private List<String> getGivenMethodNamesByClassName(String className) {
        return Optional.ofNullable(inputMethodParams).orElseGet(Collections::emptyList).stream()
                .filter(inputMethodParam->inputMethodParam.getClassName().equalsIgnoreCase(className))
                .map(MethodParameter::getMethodName)
                .distinct()
                .collect(Collectors.toList());
    }

    private static void generateAndWriteJson(String outputPath,String jsonContent){
        try{
            Path outputFile = Paths.get(outputPath);
            Files.createDirectories(outputFile.getParent());
            Files.write(outputFile, jsonContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path getRequestSheetPath(String workbookPath,String className,String methodName){
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(workbookPath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Create a workbook object
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Iterate over all the sheets in the workbook
        for (Sheet sheet : workbook) {


            // Check if the current sheet matches the desired sheet name
            if (sheet.getSheetName().equalsIgnoreCase(methodName+"_Request")) {
                // If found, return the path of the Excel file
                return Paths.get(workbookPath);
            }
        }


        // If the sheet is not found, return null
        return null;
    }

    private Map<String,String> getKeyValuePairs (String assertionString) {
        Map<String, String> map = new HashMap<>();

        String[] keyValuePairs = assertionString.split(";");

        for (String pair : keyValuePairs) {

            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            map.put(key, value);
        }

        return map;
    }

}
