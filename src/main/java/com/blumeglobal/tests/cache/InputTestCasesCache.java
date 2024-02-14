package com.blumeglobal.tests.cache;

import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.util.FileReaderUtil;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InputTestCasesCache {

    @Getter
    public static List<InputTestCases> inputTestCasesList = new ArrayList<>();

    @Getter
    public static List<String> inputMethodNamesByClassName = new ArrayList<>();


    public static void cacheInputTestCases (Path excelPath){
        Workbook workbook = null;

        try {
            workbook = new XSSFWorkbook(Files.newInputStream(excelPath));
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
        inputTestCasesList= FileReaderUtil.readExcelFile(excelPath.toString(), workbook.getSheet("TestCases"),InputTestCases.class);
    }

    public static String getRequestPropertyStringByClassNameAndMethodName (String className,String methodName) {
        for(InputTestCases inputTestCase:inputTestCasesList){
            if(inputTestCase.getClassName().equals(className) && inputTestCase.getMethodName().equals(methodName)) {
                return inputTestCase.getRequestProperties();
            }
        }
        return null;
    }


}

