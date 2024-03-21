package com.blumeglobal.tests.util;

import com.blumeglobal.tests.model.jsonEntity.ReqRes;
import com.blumeglobal.tests.model.jsonEntity.jsonReqRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    public static List<jsonReqRes> generateJsonString (Path path, String className, String methodName){

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(Files.newInputStream(path));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Sheet requestSheet = workbook.getSheet(methodName);
        List<ReqRes> dataList = getDataList(requestSheet);

        return convertToJsonString(dataList);
    }

    public static List<ReqRes> getDataList(Sheet sheet){

        List<ReqRes> dataList = new ArrayList<>();
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            List<Map<String,Object>> requestDataList = new ArrayList<>();

            Map<String, Object> requestData = new HashMap<>();
            Map<String, Object> responseData = new HashMap<>();

            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                String header = sheet.getRow(1).getCell(j).getStringCellValue();
                String apiObjectType = sheet.getRow(0).getCell(j).getStringCellValue();
                if(apiObjectType.equals("request")){
                    requestData.put(header, getCellValue(cell));
                } else {
                    responseData.put(header, getCellValue(cell));
                }
            }

            requestDataList.add(requestData);

            responseData.put("results",requestDataList);
            dataList.add(new ReqRes(requestDataList,responseData));
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

    private static List<jsonReqRes> convertToJsonString( List<ReqRes>dataList){

        List<jsonReqRes> jsonList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            for (ReqRes reqRes : dataList) {
                String requestString = objectMapper.writeValueAsString(reqRes.getRequestData());
                String responseString = objectMapper.writeValueAsString(reqRes.getResponseData());
                jsonList.add(new jsonReqRes(requestString, responseString));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonList;
    }

}
