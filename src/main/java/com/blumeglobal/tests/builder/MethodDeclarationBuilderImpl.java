package com.blumeglobal.tests.builder;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.model.excel.InputTestCases;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
                        assert parameter != null;
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

    private List<String> getAssertionParametersAsList(String inputString) {
        String[] partsArray = inputString.split(",");
        List<String> resultList = new ArrayList<>();

        for (String part : partsArray) {
            String trimmedPart = part.trim();
            resultList.add(trimmedPart);
        }

        return resultList;
    }

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

    private Parameter getJavaParserParameter(MethodParameter methodParameter, List<Parameter> parameterList) {

        for (Parameter param : parameterList) {
            if (param.getNameAsString().equals(methodParameter.getParameterName())) {
                return param;
            }
        }
        return null;
    }


    private Map<String,List<String>> getHeadersAndValidationChecksFromJson(String jsonContent) throws IOException {
        try {
            return parseJson(jsonContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, List<String>> parseJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        Map<String, List<String>> result = new HashMap<>();
        traverseJson(rootNode, result);
        return result;
    }



    private static void traverseJson(JsonNode node, Map<String, List<String>> result) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                extractPattern(field.getKey(), result);
                traverseJson(field.getValue(), result);
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                traverseJson(arrayItem, result);
            }
        }
    }

    private static void extractPattern(String key, Map<String, List<String>> result) {
        Pattern pattern = Pattern.compile("([^\\{]*)\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(key);
        List<String> matches = result.computeIfAbsent(key, k -> new ArrayList<>());
        while (matcher.find()) {
            String originalName = matcher.group(1).trim();
            String valueInsideCurlyBraces = matcher.group(2).trim();

            // Split the value inside curly braces by comma and add each part to the list
            String[] values = valueInsideCurlyBraces.split(",");
            for (String value : values) {
                matches.add(value.trim());
            }

            // Replace the key with the name before the curly braces open
            String replacedKey = key.replace(matcher.group(), originalName);
            if (!replacedKey.equals(key)) {
                // Remove the original key and add the replaced key
                List<String> value = result.remove(key);
                result.put(replacedKey, value);
            }
        }
    }









}
