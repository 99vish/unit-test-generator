package com.blumeglobal.tests.builder.interfaces;

import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.model.output.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MethodDeclarationBuilder {

    List<MethodDeclaration> buildMethodDeclarations(String className, List<InputTestCases> inputTestCasesList) throws IOException;

}
