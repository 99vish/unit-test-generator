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

    private List<InputTestCases> inputTestCases;

    private final String className;



    public ClassDeclarationBuilderImpl(MethodDeclarationBuilder methodDeclarationBuilder, List<InputTestCases> inputTestCases,String className){
        this.methodDeclarationBuilder=methodDeclarationBuilder;
        this.inputTestCases=inputTestCases;
        this.className=className;
    }

    @Override
    public TestClassDeclaration buildClassDeclarations() {


        TestClassDeclaration classDeclaration = new TestClassDeclaration();
        ClassOrInterfaceDeclaration classOrInterfaceDeclarationByClassName = Cache.getClassOrInterfaceDeclarationByClassName(className);
        classDeclaration.setClassPath(Cache.getPathFromClassDeclaration(classOrInterfaceDeclarationByClassName));
        classDeclaration.setPackageName(Cache.getPackageFromClassDeclarationNode(classOrInterfaceDeclarationByClassName.getParentNodeForChildren()));
        classDeclaration.setImports(Cache.getImportsFromClassDeclarationNode(classOrInterfaceDeclarationByClassName.getParentNodeForChildren()));
        classDeclaration.setClassName(className);
        List<MethodDeclaration> methodDeclarations = methodDeclarationBuilder.buildMethodDeclarations(className);
        classDeclaration.getMethodDeclarations().addAll(methodDeclarations);

        return classDeclaration;
    }

}
