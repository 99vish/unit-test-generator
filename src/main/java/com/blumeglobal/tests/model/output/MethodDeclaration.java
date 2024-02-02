package com.blumeglobal.tests.model.output;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MethodDeclaration {

    private String methodName;

    private List<Argument> arguments=new ArrayList<>();

    private String returnValue;

    private String exception;

    private Boolean hasRequestBody;

}
