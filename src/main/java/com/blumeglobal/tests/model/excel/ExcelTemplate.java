package com.blumeglobal.tests.model.excel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExcelTemplate {
    private List<String> headers;
    private List<Map<String, List<String>>> compulsoryAssertionParametersPerMethod;
    private List<String> requests; //this list of strings will have those methods which require only requests
    private Map<String,List<String>> methodParams;
    private String className;
    private List<String> methodsRequiringResponses; //this list of strings consist of those methods which require only responses
    private List<String> ApiResponseParameters ; //common parameters in ApiResponseEntity like successCode, errorCode, status etc
}
