package com.blumeglobal.tests.builder.interfaces;

import com.blumeglobal.tests.model.output.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.List;
import java.util.Map;

public interface MethodDeclarationBuilder {

    List<MethodDeclaration> buildMethodDeclarations(String className);

}
