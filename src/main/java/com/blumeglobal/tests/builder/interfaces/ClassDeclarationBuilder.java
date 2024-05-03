package com.blumeglobal.tests.builder.interfaces;

import com.blumeglobal.tests.model.output.TestClassDeclaration;

import java.io.IOException;
import java.util.List;

public interface ClassDeclarationBuilder {

    TestClassDeclaration buildClassDeclarations() throws IOException;

}
