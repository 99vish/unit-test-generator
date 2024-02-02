package com.blumeglobal.tests.model.excel;


import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelSheet;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ExcelSheet("TestCases")
public class InputTestCases {

    @ExcelCellName(value = "Class Name")
    @NotEmpty(message = "Class Name cannot be Empty")
    private String className;

    @ExcelCellName(value = "Method Name")
    @NotEmpty(message = "Method Name cannot be Empty")
    private String methodName;

    @ExcelCellName(value = "Expected Output")
    @NotEmpty(message = "Output cannot be Empty")
    private String expectedOutput;

}
