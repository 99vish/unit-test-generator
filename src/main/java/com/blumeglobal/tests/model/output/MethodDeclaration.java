package com.blumeglobal.tests.model.output;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MethodDeclaration {

    private String methodName;

    private List <Argument> arguments = new ArrayList<>();

    private String returnValue;

    private String exception;

    private Boolean hasRequestBody;

    private Boolean hasPathVariable;

    private Boolean hasRequestParam;

    private String entityType = null;

    private String returnEntityType = null;

    private List<String> assertionParameters ;

    private List<Map<String,String>> pathToJsonFiles;

}
