package com.blumeglobal.tests.model.output;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestClassDeclaration {

    private String packageName;

    private List<String> imports=new ArrayList<>();

    private String className;

    private List<MethodDeclaration> methodDeclarations=new ArrayList<>();

    private Path classPath;

    private Boolean isConstructorPresent;

    private List<String> dependentFieldClasses = new ArrayList<>();

}
