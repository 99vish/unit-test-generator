package com.blumeglobal.tests.model.excel;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelSheet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodParameter {


    private String className;

    private String methodName;

    private String parameterName;

    private String parameterValue;


}
