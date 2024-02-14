package com.blumeglobal.tests.model.request;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@Setter
public class ExcelPathRequest {

    private Path completedExcelPath;
}
