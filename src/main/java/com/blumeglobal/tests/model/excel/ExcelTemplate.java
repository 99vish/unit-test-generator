package com.blumeglobal.tests.model.excel;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExcelTemplate {
    private List<String> headers;
    //private List<Map<String, List<String>>> requests;
    private List<String> requests;
    private List<List<String>> methodParams;
}
