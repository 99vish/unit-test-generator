package com.blumeglobal.tests.builder;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.controller.PathController;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.jsonEntity.jsonReqRes;
import com.blumeglobal.tests.model.output.Argument;
import com.blumeglobal.tests.model.output.MethodDeclaration;
import com.blumeglobal.tests.model.excel.MethodParameter;
import com.blumeglobal.tests.builder.interfaces.MethodDeclarationBuilder;
import com.blumeglobal.tests.util.PathGeneratorUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodDeclarationBuilderImpl implements MethodDeclarationBuilder {

    //public List<MethodParameter> inputMethodParams;
    public Map<String,List<List<MethodParameter>>> methodNameToParametersMap;

    public MethodDeclarationBuilderImpl(Map<String,List<List<MethodParameter>>>methodNameToParametersMap) {
        //this.inputMethodParams = inputMethodParams;
        this.methodNameToParametersMap = methodNameToParametersMap;
    }

    @Override
    public List<MethodDeclaration> buildMethodDeclarations(String className, List<InputTestCases> inputTestCasesList) throws IOException {

        Path classPath = Cache.getPathFromClassDeclaration(Cache.getClassOrInterfaceDeclarationByClassName(className));
        List<MethodDeclaration> methodDeclarations=new ArrayList<>();
        //List<String> methodNamesByClassNameFromExcel = getGivenMethodNamesByClassName(className);

        for (int i=0;i<inputTestCasesList.size();i++) {

            String methodName = inputTestCasesList.get(i).getMethodName();
            com.github.javaparser.ast.body.MethodDeclaration javaParserMethodDeclaration = Cache.getMethodDeclaration(className, methodName);

            List<List<MethodParameter>> methodParameterRows = methodNameToParametersMap.get(methodName);

            for (int j = 0; j < methodParameterRows.size(); j++) {

                MethodDeclaration methodDeclaration = new MethodDeclaration();
                methodDeclaration.setMethodName(methodName);
                methodDeclaration.setMethodNumber(j);
                String assertionParameters = InputTestCasesCache.getAssertionParametersStringByClassNameAndMethodName(className, methodName, inputTestCasesList);
                List<String> assertionParametersList = new ArrayList<>();

                if (assertionParameters != null) {
                    assertionParametersList = getAssertionParametersAsList(assertionParameters);
                }

                methodDeclaration.setAssertionParameters(assertionParametersList);
                assert javaParserMethodDeclaration != null;
                if (javaParserMethodDeclaration.getType().isClassOrInterfaceType()) {
                    if (javaParserMethodDeclaration.getType().asClassOrInterfaceType().getTypeArguments().isPresent()) {
                        methodDeclaration.setReturnValue(javaParserMethodDeclaration.getType().asClassOrInterfaceType().getTypeArguments().get().get(0).asString()); //it is the value inside this angular bracket ResponseEntity<" ">
                    }
                } else if (javaParserMethodDeclaration.getType().isWildcardType()) {
                    methodDeclaration.setReturnValue("Object");
                } else if (javaParserMethodDeclaration.getType().isVoidType()) {
                    methodDeclaration.setReturnValue("Void");
                } else if (javaParserMethodDeclaration.getType().isPrimitiveType()) {
                    methodDeclaration.setReturnValue(javaParserMethodDeclaration.getType().asClassOrInterfaceType().getTypeArguments().get().get(0).asString()); //it is the value inside this angular bracket ResponseEntity<" ">
                }

                if (javaParserMethodDeclaration.getType().isClassOrInterfaceType()) {
                    Optional<NodeList<Type>> typeArguments = javaParserMethodDeclaration.getType().asClassOrInterfaceType().getTypeArguments();
                    if (typeArguments.isPresent() && !typeArguments.get().isEmpty()) {
                        Type firstTypeArgument = typeArguments.get().get(0);
                        if (firstTypeArgument.isClassOrInterfaceType()) {
                            if (firstTypeArgument.asClassOrInterfaceType().getName().asString().equals("ApiResponse")) {
                                methodDeclaration.setIsApiResponsePresent(true);
                                if (firstTypeArgument.asClassOrInterfaceType().getTypeArguments().isPresent()) {
                                    Type secondTypeArgument = firstTypeArgument.asClassOrInterfaceType().getTypeArguments().get().get(0);
                                    if (secondTypeArgument.isClassOrInterfaceType()) {
                                        methodDeclaration.setResponseEntityType(secondTypeArgument.asString());
                                    }
                                }
                            }
                        } else if (firstTypeArgument.isWildcardType()) {
                            // Handle wildcard type
                            if (firstTypeArgument.asWildcardType().getExtendedType().isPresent()) {
                                if (firstTypeArgument.asWildcardType().getExtendedType().get().stream().anyMatch(t -> t instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) t).getName().asString().equals("ApiResponse"))) {
                                    methodDeclaration.setIsApiResponsePresent(true);
                                }
                            }
                        }
                    } //typeArguments not present
                } //Not a class or interface type

                List<MethodParameter> methodParams = methodParameterRows.get(j);

                NodeList<Parameter> parameterNodeList = javaParserMethodDeclaration.getParameters();
                List<Parameter> parameterList = new ArrayList<>(parameterNodeList);

                for(Parameter parameter: parameterList){


                    if (parameter.getAnnotations().isNonEmpty() && parameter.getAnnotations().get(0).getNameAsString().equals("RequestBody")) {
                        Argument argument = new Argument();
                        methodDeclaration.getArguments().add(argument);
                        String parameterName = parameter.getName().getIdentifier();
                        argument.setName(parameterName);
                        argument.setValue("value");
                        argument.setHasAnnotation(true);
                        argument.setAnnotationType("RequestBody");
                        methodDeclaration.setHasRequestBody(true);

                        Type parameterType = parameter.getType();
                        if (parameterType instanceof ClassOrInterfaceType) {
                            ClassOrInterfaceType type = (ClassOrInterfaceType) parameterType;
                            argument.setDataType(type.getName().getIdentifier());
                        } else if (parameterType != null) {
                            PrimitiveType type = (PrimitiveType) parameterType;
                            argument.setDataType(type.getType().asString());
                        } else {
                            // Handle other types as needed
                            argument.setDataType("Unknown");
                        }

                        if (parameterType instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) parameter.getType()).getName().getIdentifier().equals("ApiRequest")) {
                            argument.setIsApiRequest(true);
                            methodDeclaration.setIsApiRequestPresent(true);
                            if (((ClassOrInterfaceType) parameter.getType()).getTypeArguments().isPresent()) {
                                methodDeclaration.setRequestEntityType(((ClassOrInterfaceType) parameter.getType()).getTypeArguments().get().get(0).asClassOrInterfaceType().asString());
                                if (methodDeclaration.getIsApiResponsePresent() && methodDeclaration.getResponseEntityType() == null) {
                                    methodDeclaration.setResponseEntityType(methodDeclaration.getRequestEntityType());
                                }
                            } else {
                                methodDeclaration.setRequestEntityType("RequestEntity");
                                if (methodDeclaration.getResponseEntityType() == null) {
                                    methodDeclaration.setResponseEntityType("responseEntity");
                                }
                            }
                        } else {
                            argument.setDataType(((ClassOrInterfaceType) parameter.getType()).getName().getIdentifier());
                        }
                        //if there is a request body present then the entity inside the requestBody should be used to take the entity type
                        //methodDeclaration.setEntityType(parameter.getType().asClassOrInterfaceType().getChildNodes().get(1).toString());

                    }



                    if (methodDeclaration.getResponseEntityType() == null) {
                        methodDeclaration.setResponseEntityType("responseEntity");
                    }
                    if (methodDeclaration.getRequestEntityType() == null) {
                        methodDeclaration.setRequestEntityType("RequestEntity");
                    }



                }

                for (MethodParameter methodParam : methodParams) {

                    if (!(methodParam.getParameterName().equals("Request") || methodParam.getParameterName().equals("Response"))) {
                        Argument argument = new Argument();
                        Parameter parameter = getJavaParserParameter(methodParam, parameterList);
                        String parameterName = parameter.getName().getIdentifier();
                        argument.setName(parameterName);

                        Type parameterType = parameter.getType();
                        if (parameterType instanceof ClassOrInterfaceType) {
                            ClassOrInterfaceType type = (ClassOrInterfaceType) parameterType;
                            argument.setDataType(type.getName().getIdentifier());
                        } else if (parameterType != null) {
                            PrimitiveType type = (PrimitiveType) parameterType;
                            argument.setDataType(type.getType().asString());
                        } else {
                            argument.setDataType("Unknown");
                        }

                        argument.setValue(methodParam.getParameterValue());

                        if (parameter.getAnnotations().isNonEmpty() && parameter.getAnnotations().get(0).getNameAsString().equals("RequestParam")) {
                            argument.setHasAnnotation(true);
                            methodDeclaration.setHasRequestParam(true);
                            argument.setAnnotationType("RequestParam");
                        }

                        if (parameter.getAnnotations().isNonEmpty() && parameter.getAnnotations().get(0).getNameAsString().equals("PathVariable")) {
                            argument.setHasAnnotation(true);
                            methodDeclaration.setHasPathVariable(true);
                            argument.setAnnotationType("PathVariable");
                        }


                        methodDeclaration.getArguments().add(argument);


                    } else if (methodParam.getParameterName().equals("Request")) {

                        Path pathToJsonDirectory = PathGeneratorUtil.getPathForJsonRequestGeneration(classPath, className, methodName);
                        methodDeclaration.setPathToRequestJson(writeJson(pathToJsonDirectory.toString(), methodParam.getParameterValue(), j, "request"));
                        methodDeclaration.setHeadersAndValidationChecks(getHeadersAndValidationChecksFromJson(methodParam.getParameterValue()));


                    } else {

                        Path pathToJsonDirectory = PathGeneratorUtil.getPathForJsonRequestGeneration(classPath, className, methodName);
                        methodDeclaration.setPathToResponseJson(writeJson(pathToJsonDirectory.toString(), methodParam.getParameterValue(), j, "response"));
                        methodDeclaration.setResultHeadersAndValidationChecks(getHeadersAndValidationChecksFromJson(methodParam.getParameterValue()));
                    }


                    if (methodDeclaration.getReturnValue() == null) {
                        methodDeclaration.setReturnValue("Object");
                    }


                }
                methodDeclarations.add(methodDeclaration);
            }
        }

        return methodDeclarations;
    }



//                if(!parameterList.isEmpty()) {
//                    for (Parameter parameter : parameterList) {
//
//                        Argument argument = new Argument();
//                        String parameterName = parameter.getName().getIdentifier();
//                        argument.setName(parameterName);
//
//                        Type parameterType = parameter.getType();
//                        if (parameterType instanceof ClassOrInterfaceType) {
//                            ClassOrInterfaceType type = (ClassOrInterfaceType) parameterType;
//                            argument.setDataType(type.getName().getIdentifier());
//                        } else if (parameterType != null) {
//                            PrimitiveType type = (PrimitiveType) parameterType;
//                            argument.setDataType(type.getType().asString());
//                        } else {
//                            // Handle other types as needed
//                            argument.setDataType("Unknown");
//                        }
//
//
//                        if(parameter.getAnnotations().isNonEmpty() && parameter.getAnnotations().get(0).getNameAsString().equals("RequestBody")) {
//                            argument.setHasAnnotation(true);
//                            argument.setAnnotationType("RequestBody");
//                            methodDeclaration.setHasRequestBody(true);
//                            if(parameterType instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) parameter.getType()).getName().getIdentifier().equals("ApiRequest")){
//                                argument.setIsApiRequest(true);
//                                methodDeclaration.setIsApiRequestPresent(true);
//                                if( ((ClassOrInterfaceType) parameter.getType()).getTypeArguments().isPresent()){
//                                    methodDeclaration.setRequestEntityType(((ClassOrInterfaceType) parameter.getType()).getTypeArguments().get().get(0).asClassOrInterfaceType().asString());
//                                    if(methodDeclaration.getIsApiResponsePresent() && methodDeclaration.getResponseEntityType()==null){
//                                        methodDeclaration.setResponseEntityType(methodDeclaration.getRequestEntityType());
//                                    }
//                                } else {
//                                    methodDeclaration.setRequestEntityType("RequestEntity");
//                                    if(methodDeclaration.getResponseEntityType()==null){
//                                        methodDeclaration.setResponseEntityType("responseEntity");
//                                    }
//                                }
//                            } else {
//                                argument.setDataType(((ClassOrInterfaceType) parameter.getType()).getName().getIdentifier());
//                            }
//                            //if there is a request body present then the entity inside the requestBody should be used to take the entity type
//                            //methodDeclaration.setEntityType(parameter.getType().asClassOrInterfaceType().getChildNodes().get(1).toString());
//
//                        }
//
//                        if(methodDeclaration.getResponseEntityType()==null){
//                            methodDeclaration.setResponseEntityType("responseEntity");
//                        }
//                        if(methodDeclaration.getRequestEntityType()==null){
//                            methodDeclaration.setRequestEntityType("RequestEntity");
//                        }
//
//                        if(parameter.getAnnotations().isNonEmpty() && parameter.getAnnotations().get(0).getNameAsString().equals("RequestParam"))
//                        {
//                            argument.setHasAnnotation(true);
//                            methodDeclaration.setHasRequestParam(true);
//                            argument.setAnnotationType("RequestParam");
//                        }
//
//                        if(parameter.getAnnotations().isNonEmpty() && parameter.getAnnotations().get(0).getNameAsString().equals("PathVariable"))
//                        {
//                            argument.setHasAnnotation(true);
//                            methodDeclaration.setHasPathVariable(true);
//                            argument.setAnnotationType("PathVariable");
//                        }
//
//                        String parameterValue = getMethodParameter(className, javaParserMethodDeclaration.getNameAsString(), parameter.getNameAsString());
//
//
//                        if (argument.getIsApiRequest()) {
//
//                            Path pathToJsonDirectory = PathGeneratorUtil.getPathForJsonRequestGeneration(classPath,className,methodName);
//                            List<Map<String,String>> jsonRequestResponsePaths = new ArrayList<>();
//                            ExcelToJsonDataGeneratorUtil excelToJsonDataGeneratorUtil = new ExcelToJsonDataGeneratorUtil();
//                            List<jsonReqRes> jsonReqResList = excelToJsonDataGeneratorUtil.generateJsonString(excelPath,className,methodName);
//                            methodDeclaration.setResultHeadersAndValidationChecks(excelToJsonDataGeneratorUtil.getResultHeadersAndValidationChecks());
//                            methodDeclaration.setHeadersAndValidationChecks(excelToJsonDataGeneratorUtil.getHeadersAndValidationChecks());
//                            generateAndWriteJson(pathToJsonDirectory.toString(),jsonReqResList,jsonRequestResponsePaths);
//                            methodDeclaration.setPathToJsonFiles(jsonRequestResponsePaths);
//
//
//                        }
//                        else {
//                            argument.setValue(parameterValue);
//                        }
//
//                        if(!methodDeclaration.getIsApiRequestPresent() && methodDeclaration.getIsApiResponsePresent()){
//                            Path pathToJsonDirectory = PathGeneratorUtil.getPathForJsonResponseGeneration(classPath,className,methodName);
//                            ExcelToJsonDataGeneratorUtil excelToJsonDataGeneratorUtil = new ExcelToJsonDataGeneratorUtil();
//                            String responseJson = excelToJsonDataGeneratorUtil.generateResponseJsonString(excelPath,className,methodName);
//                            try {
//                                methodDeclaration.setPathToResponseJson(generateAndWriteResponseJson(pathToJsonDirectory.toString(),responseJson));
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//
//                        }
//
//
//                    }
//                }
//
//
//            }


















//    private String getMethodParameter(String className, String methodName, String parameterName) {
//        for (MethodParameter parameter : inputMethodParams) {
//            if (parameter.getClassName().equalsIgnoreCase(className) && parameter.getMethodName().equalsIgnoreCase(methodName) && parameter.getParameterName().equalsIgnoreCase(parameterName)) {
//                return parameter.getParameterValue();
//            }
//        }
//        return null;
//    }

    private List<String> getAssertionParametersAsList(String inputString) {
        String[] partsArray = inputString.split(",");
        List<String> resultList = new ArrayList<>();

        for (String part : partsArray) {
            String trimmedPart = part.trim();
            resultList.add(trimmedPart);
        }

        return resultList;
    }

//    private List<String> getGivenMethodNamesByClassName(String className) {
//        return Optional.ofNullable(inputMethodParams).orElseGet(Collections::emptyList).stream()
//                .filter(inputMethodParam->inputMethodParam.getClassName().equalsIgnoreCase(className))
//                .map(MethodParameter::getMethodName)
//                .distinct()
//                .collect(Collectors.toList());
//    }


    private static String writeJson(String outputPath, String content, int j ,String type){
        try {
            Path outputFile = Paths.get(outputPath);
            Files.createDirectories(outputFile.getParent());

            Path requiredFile = outputFile.resolve(type + "_" + j + ".json");

            Files.createDirectories(requiredFile.getParent());
            Files.write(requiredFile, content.getBytes());

            return requiredFile.toString().replace("\\","\\\\");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void generateAndWriteJson(String outputPath,List<jsonReqRes> jsonReqResList,List<Map<String,String>>jsonRequestResponsePath){
        try{
            Path outputFile = Paths.get(outputPath);
            Files.createDirectories(outputFile.getParent());

            for (int i = 0; i < jsonReqResList.size(); i++) {

                jsonReqRes jsonReqResObj = jsonReqResList.get(i);
                String requestContent = jsonReqResObj.getRequestJson();
                String responseContent = jsonReqResObj.getReponseJson();

                Path requestFile = outputFile.resolve("request_" + i + ".json");
                Path responseFile = outputFile.resolve("response_" + i + ".json");


                // Create the parent directories if not already present
                Files.createDirectories(requestFile.getParent());
                Files.createDirectories(responseFile.getParent());


                Files.write(requestFile, requestContent.getBytes());
                Files.write(responseFile, responseContent.getBytes());

                Map<String, String> entry = new HashMap<>();
                entry.put(requestFile.toString().replace("\\","\\\\"),responseFile.toString().replace("\\","\\\\"));
                jsonRequestResponsePath.add(entry);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path getRequestSheetPath(String workbookPath){
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

        Sheet sheet = workbook.getSheet("RequestsAndResponses");
        if (sheet != null) {
            // If found, return the path of the Excel file
            return Paths.get(workbookPath);
        }


        // If the sheet is not found, return null
        return null;
    }

    private static Path getMethodSpecificationsSheet(String workbookPath){
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

        Sheet sheet = workbook.getSheet("MethodDetails");
        if (sheet != null) {
            // If found, return the path of the Excel file
            return Paths.get(workbookPath);
        }


        // If the sheet is not found, return null
        return null;
    }

    private static String generateAndWriteResponseJson(String outputPath,String responseJsonString) throws IOException {
        Path outputFile = Paths.get(outputPath);
        try {
            Files.createDirectories(outputFile.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path responseFile = outputFile.resolve("response.json");

        try {
            Files.createDirectories(responseFile.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Files.write(responseFile, responseJsonString.getBytes());

        return responseFile.toString().replace("\\","\\\\");
    }


    private Parameter getJavaParserParameter(MethodParameter methodParameter, List<Parameter> parameterList) {

        for (Parameter param : para







}
