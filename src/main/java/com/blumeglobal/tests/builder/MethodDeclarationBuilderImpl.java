package com.blumeglobal.tests.builder;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.model.output.Argument;
import com.blumeglobal.tests.model.output.MethodDeclaration;
import com.blumeglobal.tests.model.excel.MethodParameter;
import com.blumeglobal.tests.builder.interfaces.MethodDeclarationBuilder;
import com.blumeglobal.tests.util.PathGeneratorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodDeclarationBuilderImpl implements MethodDeclarationBuilder {

    private List<MethodParameter> inputMethodParams;

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
            MethodDeclaration methodDeclaration=new MethodDeclaration();
            methodDeclaration.setMethodName(methodName);
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

                    if(argument.getDataType().equals("ApiRequest")) {
                        argument.setEntityType(type.asClassOrInterfaceType().getTypeArguments().get().get(0).asString());
                    }

                    MethodParameter methodParameter = getMethodParameter(className, javaParserMethodDeclaration.getNameAsString(), parameter.getNameAsString());
                    if (argument.getName().equals("apiRequest")) {
                        methodDeclaration.setHasRequestBody(true);
                        argument.setValue("apijson");
                        Path pathToJsonFile = PathGeneratorUtil.getPathForJsonRequestGeneration(classPath,className);
                        argument.setPathToJsonFile(pathToJsonFile.toString().replace("\\","\\\\"));

                        generateAndWriteJson(pathToJsonFile.toString(),methodParameter.getParameterValue());
                    }
                    else {
                        argument.setValue(methodParameter.getParameterValue());
                    }
                    methodDeclaration.getArguments().add(argument);
                }
            }
            methodDeclarations.add(methodDeclaration);
        }
        return methodDeclarations;
    }

    private MethodParameter getMethodParameter(String className, String methodName, String parameterName) {
        MethodParameter methodParameter = null;
        for (MethodParameter parameter : inputMethodParams) {
            if (parameter.getClassName().equalsIgnoreCase(className) && parameter.getMethodName().equalsIgnoreCase(methodName) && parameter.getParameterName().equalsIgnoreCase(parameterName)) {
                methodParameter = parameter;
                break;
            }
        }
        return methodParameter;
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

}
