package com.blumeglobal.tests.model.excel;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelSheet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ExcelSheet("MethodParams")
public class MethodParameter {

    @ExcelCellName("Class Name")
    private String className;

    @ExcelCellName("Method Name")
    private String methodName;

    @ExcelCellName("Parameter Name")
    private String parameterName;

    @ExcelCellName("Parameter Value")
    private String parameterValue;


}
