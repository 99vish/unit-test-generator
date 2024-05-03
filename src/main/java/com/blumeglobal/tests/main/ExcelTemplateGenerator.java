package com.blumeglobal.tests.main;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.model.excel.ExcelTemplate;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import lombok.Getter;

import java.util.*;

public class ExcelTemplateGenerator {



//    @Getter
//    public static List<List<String>> methodParams = new ArrayList<>();


    public static ExcelTemplate generateExcelTemplate( String ClassName ,List<InputTestCases> InputTestCasesList){

        List<String>Headers=Arrays.asList("Class Name","Method Name","Parameter Name","Parameter Value");
        List<String>Requests=new ArrayList<>();

        List<String>Responses = new ArrayList<>();

        Map<String,List<String>> methodParams = new HashMap<>();

        for(InputTestCases inputTestCase:InputTestCasesList){

            List<String>Parameters = new ArrayList<>();
            boolean hasApiResponse = false;
            MethodDeclaration methodDeclaration = Cache.getMethodDeclaration(ClassName,inputTestCase.getMethodName());
            assert methodDeclaration != null;
            if(methodDeclaration.getType().isClassOrInterfaceType()) {
                Optional<NodeList<Type>> typeArguments = methodDeclaration.getType().asClassOrInterfaceType().getTypeArguments();
                if (typeArguments.isPresent() && !typeArguments.get().isEmpty()) {
                    Type firstTypeArgument = typeArguments.get().get(0);
                    if (firstTypeArgument.isClassOrInterfaceType()) {
                        if (firstTypeArgument.asClassOrInterfaceType().getName().asString().equals("ApiResponse")) {
                            hasApiResponse = true;
                        }
                    } else if (firstTypeArgument.isWildcardType()) {
                        // Handle wildcard type
                        if (firstTypeArgument.asWildcardType().getExtendedType().isPresent()){
                            if (firstTypeArgument.asWildcardType().getExtendedType().get().stream().anyMatch(t -> t instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) t).getName().asString().equals("ApiResponse"))){
                                hasApiResponse = true;
                            }
                        }
                    }
                } //typeArguments not present
            } //Not a class or interface type
            NodeList<Parameter> parameterNodeList = null;
            try {
                parameterNodeList = methodDeclaration.getParameters();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            List<Parameter> parameterList = new ArrayList<>(parameterNodeList);

            if (!parameterList.isEmpty()) {
                for (Parameter parameter : parameterList) {
                    List<String> row = new ArrayList<>();

//                    if (parameter.getAnnotations().get(0).getNameAsString().equals("RequestBody")) {
//                        String entity = methodDeclaration.getType().asClassOrInterfaceType().getChildNodes().get(1).getChildNodes().get(1).toString();
//                        ClassOrInterfaceDeclaration entityClassDeclaration = Cache.getClassOrInterfaceDeclarationByClassName(entity);
//                        List<FieldDeclaration> fields = entityClassDeclaration.getFields();
//                        List<String>fieldNames = new ArrayList<>();
//                        for(FieldDeclaration field:fields){
//                            fieldNames.add(field.getVariable(0).getNameAsString());
//                        }
//                        Map<String,List<String>>request=new HashMap<>();
//                        request.put(inputTestCase.getMethodName(),fieldNames);
//                        Requests.add(request);
//                    }
//                    else{
                    if (parameter.getAnnotations().isNonEmpty() && parameter.getAnnotations().get(0).getNameAsString().equals("RequestBody")) {
                        Type parameterType = parameter.getType();
                        if (parameterType instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) parameter.getType()).getName().getIdentifier().equals("ApiRequest")) {
                            Requests.add(methodDeclaration.getNameAsString()); //if ApiRequest is present then only we need excel;
                        }
                        if (hasApiResponse) {  //if this method is being added in requests then we don't need this methodName in response
                            hasApiResponse = false;
                        }
                    } else {
                        Parameters.add(parameter.getNameAsString());
//                        row.add(inputTestCase.getClassName());
//                        row.add(inputTestCase.getMethodName());
//                        row.add(parameter.getNameAsString());
//                        methodParams.add(row);
                    }


                }
                methodParams.put(inputTestCase.getMethodName(),Parameters);
            }

            if (hasApiResponse) {
                Responses.add(methodDeclaration.getNameAsString());
            }
        }

        List<String> apiResponseParameters = new ArrayList<>();

        apiResponseParameters.add("correlationId");
        apiResponseParameters.add("status");
        apiResponseParameters.add("successMessage");
        apiResponseParameters.add("errorMessage");
        apiResponseParameters.add("successCode");
        apiResponseParameters.add("errorCode");
        apiResponseParameters.add("pageableInfo");




        ExcelTemplate excelTemplate = new ExcelTemplate();
        excelTemplate.setClassName(ClassName);
        excelTemplate.setHeaders(Headers);
        excelTemplate.setMethodParams(methodParams);
        excelTemplate.setRequests(Requests);
        excelTemplate.setMethodsRequiringResponses(Responses);
        excelTemplate.setApiResponseParameters(apiResponseParameters);

        return excelTemplate;


    }
}
