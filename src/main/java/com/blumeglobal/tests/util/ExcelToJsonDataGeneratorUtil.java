package com.blumeglobal.tests.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelToJsonDataGeneratorUtil {

    public static String generateJsonString (Path path,String className,String methodName){

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(Files.newInputStream(path));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Sheet requestSheet = workbook.getSheet(methodName+"_Request");
        List<Map<String, Object>> dataList = getDataList(requestSheet);

        return convertToJsonString(dataList);
    }

    public static List<Map<String,Object>> getDataList(Sheet sheet){

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            Map<String, Object> rowData = new HashMap<>();

            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                String header = sheet.getRow(0).getCell(j).getStringCellValue();
                rowData.put(header, getCellValue(cell));
            }

            dataList.add(rowData);
        }
        return dataList;
    }

    private static Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private static String convertToJsonString(List<Map<String, Object>> dataList){

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString=null;
        try {
            jsonString = objectMapper.writeValueAsString(dataList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonString;
    }

}
