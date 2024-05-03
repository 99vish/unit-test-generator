package com.blumeglobal.tests.model.output;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MethodDeclaration {

    private String methodName;

    private List <Argument> arguments = new ArrayList<>();

    private String returnValue;

    private String exception;

    private Boolean hasRequestBody = false;

    private Boolean hasPathVariable;

    private Boolean hasRequestParam;

    private List<String> assertionParameters ;

    private Boolean isApiResponsePresent = false;

    private String pathToResponseJson ; //for cases when there is no request json, only response json is present

    private String requestEntityType = null;

    private String responseEntityType = null;

    private Boolean isApiRequestPresent = false;

    private Boolean isResponseWildCard = false;

    private String pathToRequestJson;

    private Map<String,List<String>> resultHeadersAndValidationChecks;

    private Map<String,List<String>> headersAndValidationChecks;

    private Integer methodNumber;


}
