package com.blumeglobal.tests.builder;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.model.output.TestClassDeclaration;
import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.output.MethodDeclaration;
import com.blumeglobal.tests.builder.interfaces.ClassDeclarationBuilder;
import com.blumeglobal.tests.builder.interfaces.MethodDeclarationBuilder;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.*;

public class ClassDeclarationBuilderImpl implements ClassDeclarationBuilder {

    private final MethodDeclarationBuilder methodDeclarationBuilder;

    private Map<String, List<InputTestCases>> inputTestCasesMap;


    public ClassDeclarationBuilderImpl(MethodDeclarationBuilder methodDeclarationBuilder, Map<String, List<InputTestCases>> inputTestCasesMap){
        this.methodDeclarationBuilder=methodDeclarationBuilder;
        this.inputTestCasesMap=inputTestCasesMap;
    }

    @Override
    public List<TestClassDeclaration> buildClassDeclarations() {
        Set<String> classNames = inputTestCasesMap.keySet();
        List<TestClassDeclaration> classDeclarations = new ArrayList<>();

        for (String className : classNames) {
            TestClassDeclaration classDeclaration = new TestClassDeclaration();
            ClassOrInterfaceDeclaration classOrInterfaceDeclarationByClassName = Cache.getClassOrInterfaceDeclarationByClassName(className);
            classDeclaration.setClassPath(Cache.getPathFromClassDeclaration(classOrInterfaceDeclarationByClassName));
            classDeclaration.setPackageName(Cache.getPackageFromClassDeclarationNode(classOrInterfaceDeclarationByClassName.getParentNodeForChildren()));
            classDeclaration.setImports(Cache.getImportsFromClassDeclarationNode(classOrInterfaceDeclarationByClassName.getParentNodeForChildren()));
            classDeclaration.setClassName(className);
            List<MethodDeclaration> methodDeclarations = methodDeclarationBuilder.buildMethodDeclarations(className);
            classDeclaration.getMethodDeclarations().addAll(methodDeclarations);
            classDeclarations.add(classDeclaration);
        }

        return classDeclarations;
    }

}
