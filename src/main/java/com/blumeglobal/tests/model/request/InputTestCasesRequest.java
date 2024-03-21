package com.blumeglobal.tests.model.request;

import com.blumeglobal.tests.model.excel.InputTestCases;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InputTestCasesRequest {

    private List<InputTestCases> inputTestCasesList =new ArrayList<>();
}
