package com.blumeglobal.tests.cache;

import com.blumeglobal.tests.model.excel.InputTestCases;
import com.blumeglobal.tests.util.FileReaderUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InputTestCasesCache {




    public void cacheInputTestCases (Path excelPath, List<InputTestCases> inputTestCasesList, Map<String,List<InputTestCases>> inputTestCasesByClassNameMap, List<String> inputMethodNamesByClassName, List<String> inputClassNames){
        Workbook workbook = null;

        try {
            workbook = new XSSFWorkbook(Files.newInputStream(excelPath));
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
        inputTestCasesList= FileReaderUtil.readExcelFile(excelPath.toString(), workbook.getSheet("TestCases"),InputTestCases.class);

        populateInputTestCasesByClassNameMap(inputTestCasesList,inputTestCasesByClassNameMap,inputClassNames);


    }

    public static String getAssertionParametersStringByClassNameAndMethodName (String className,String methodName,List<InputTestCases> inputTestCasesList) {
        for(InputTestCases inputTestCase:inputTestCasesList){
            if(inputTestCase.getClassName().equals(className) && inputTestCase.getMethodName().equals(methodName)) {
                return inputTestCase.getAssertionParametersString();
            }
        }
        return null;
    }

    public void populateInputTestCasesByClassNameMap(List<InputTestCases> inputTestCasesList,Map<String,List<InputTestCases>> inputTestCasesByClassNameMap, List<String> inputClassNames){
        for(InputTestCases inputTestCase:inputTestCasesList) {
            String className = inputTestCase.getClassName();


            if (inputTestCasesByClassNameMap.containsKey(className)) {
                inputTestCasesByClassNameMap.get(className).add(inputTestCase);
            } else {
                inputTestCasesByClassNameMap.put(className, new ArrayList<>());
                inputTestCasesByClassNameMap.get(className).add(inputTestCase);
                inputClassNames.add(className);
            }
        }
    }



}

