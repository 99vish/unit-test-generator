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
import lombok.Getter;

import java.util.*;

public class ExcelTemplateGenerator {



//    @Getter
//    public static List<String> Headers =new ArrayList<>();
//    @Getter
//    public static List<String> Requests = new ArrayList<>();

    @Getter
    public static List<List<String>> methodParams = new ArrayList<>();

    public static ExcelTemplate generateExcelTemplate( String ClassName){

        List<String>Headers=Arrays.asList("Class Name","Method Name","Parameter Name","Parameter Value");
        List<String>Requests=new ArrayList<>();
        List<List<String>>methodParams = new ArrayList<>();


        List<InputTestCases>InputTestCasesList = InputTestCasesCache.getInputTestCasesByClassName(ClassName);

        for(InputTestCases inputTestCase:InputTestCasesList){


            MethodDeclaration methodDeclaration = Cache.getMethodDeclaration(ClassName,inputTestCase.getMethodName());
            NodeList<Parameter> parameterNodeList = null;
            try {
                parameterNodeList = methodDeclaration.getParameters();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            List<Parameter> parameterList = new ArrayList<>(parameterNodeList);
            if(!parameterList.isEmpty()) {
                for (Parameter parameter : parameterList) {
                    List<String> row= new ArrayList<>();

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
                    if(parameter.getAnnotations().get(0).getNameAsString().equals("RequestBody")) {
                        Requests.add(methodDeclaration.getNameAsString());
                    }
                    else {
                        row.add(inputTestCase.getClassName());
                        row.add(inputTestCase.getMethodName());
                        row.add(parameter.getNameAsString());
                        //row.add("");
                        methodParams.add(row);
                    }


                }
            }
        }

        ExcelTemplate excelTemplate = new ExcelTemplate();
        excelTemplate.setClassName(ClassName);
        excelTemplate.setHeaders(Headers);
        excelTemplate.setMethodParams(methodParams);
        excelTemplate.setRequests(Requests);

        return excelTemplate;


    }
}
