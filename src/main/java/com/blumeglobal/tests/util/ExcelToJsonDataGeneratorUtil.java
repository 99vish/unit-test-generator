package com.blumeglobal.tests.util;

import com.blumeglobal.tests.model.jsonEntity.ReqRes;
import com.blumeglobal.tests.model.jsonEntity.jsonReqRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.github.javaparser.JavaToken.Kind.NULL;

public class ExcelToJsonDataGeneratorUtil {


    @Getter
    public Map<String,List<String>>resultHeadersAndValidationChecks = new HashMap<>();//headers present in "results" variable of apiResponse along with their validation check parameters

    @Getter
    public Map<String,List<String>>headersAndValidationChecks = new HashMap<>(); //headers different from results in apiResponse along with their validation check parameters



    public List<jsonReqRes> generateJsonString (Path path, String className, String methodName){

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(Files.newInputStream(path));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Sheet requestSheet = workbook.getSheet("RequestsAndResponses");


        int[] rowNums = getFilteredRows(requestSheet,className,methodName);
        List<ReqRes> dataList = getDataList(requestSheet,rowNums[0],rowNums[1]);

        return convertToJsonString(dataList);
    }

    public List<ReqRes> getDataList(Sheet sheet,int startingRowNum, int endingRowNum){

        List<ReqRes> dataList = new ArrayList<>();

        //everything must start from second column

        for(int k=2; k<sheet.getRow(startingRowNum).getLastCellNum();k++){
            String header = sheet.getRow(startingRowNum+1).getCell(k).getStringCellValue();  //headerName
            String apiObjectType = sheet.getRow(startingRowNum).getCell(k).getStringCellValue();   //type of apiObject

            String headerName ;
            List<String> headerValidations = new ArrayList<>();

            if (header.contains("{") && header.contains("}")) {
                // Splitting the input string into two parts: name and validations
                String[] parts = header.split("\\{", 2);
                headerName = parts[0];
                String validationsString = parts[1].substring(0, parts[1].length() - 1);
                headerValidations = Arrays.asList(validationsString.split(","));
            } else {
                headerName=header;  // No validations specified
            }
            if(apiObjectType.equals("request")){
                resultHeadersAndValidationChecks.put(headerName,headerValidations);
            } else {
                headersAndValidationChecks.put(headerName,headerValidations);
            }
        }
        for (int i = startingRowNum+2 ; i <= endingRowNum; i++) {
            Row row = sheet.getRow(i);

            List<Map<String,Object>> requestDataList = new ArrayList<>();

            Map<String, Object> requestData = new HashMap<>();
            Map<String, Object> responseData = new HashMap<>();

            for (int j = 2; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                String header = sheet.getRow(startingRowNum+1).getCell(j).getStringCellValue();
                String apiObjectType = sheet.getRow(startingRowNum).getCell(j).getStringCellValue();

                String headerName;
                if (header.contains("{") && header.contains("}")) {
                    String[] parts = header.split("\\{", 2);
                    headerName = parts[0];
                } else {
                    headerName = header;
                }

                if(cell != null) {
                    if (apiObjectType.equals("request")) {
                        //handling the nested part
                        if(headerName.contains(".")) {
                            String[] nestedHeaders = headerName.split("\\.");
                            Map<String, Object> nestedJson = new HashMap<>();
                            Map<String,Object> currentLevel = nestedJson;
                            for(int k = 0;k<nestedHeaders.length;k++){
                                String key = nestedHeaders[k];
                                currentLevel.put(key,new HashMap<>());
                                currentLevel = (Map<String, Object>) currentLevel.get(key);
                            }
                            currentLevel.put(nestedHeaders[nestedHeaders.length - 1], getCellValue(cell));
                            requestData.put(headerName, nestedJson);
                        } else {
                            requestData.put(headerName, getCellValue(cell));
                        }
                    } else {
                        responseData.put(headerName, getCellValue(cell));
                    }
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

    private Map<String,Object> getResponseDataList(Sheet sheet,int startingRowNum,int endingRowNum){

        for(int k=2;k<sheet.getRow(startingRowNum).getLastCellNum();k++) {
            String header = sheet.getRow(startingRowNum+1).getCell(k).getStringCellValue();

            String headerName ;
            List<String> headerValidations = new ArrayList<>();
            if (header.contains("{") && header.contains("}")) {
                // Splitting the input string into two parts: name and validations
                String[] parts = header.split("\\{", 2);
                headerName = parts[0];
                String validationsString = parts[1].substring(0, parts[1].length() - 1);
                headerValidations = Arrays.asList(validationsString.split(","));
            } else {
                // No validations specified
                headerName=header;
            }
            if(headerName.contains(".")){
                String[] nestedHeaders = headerName.split("\\.");
                for(int i=0;i<nestedHeaders.length;i++){
                    if(nestedHeaders[i].equals("results")){
                        resultHeadersAndValidationChecks.put(headerName,headerValidations);
                    }
                }
            } else {
                headersAndValidationChecks.put(headerName,headerValidations);
            }
        }
        Map<String,Object> responseDataList = new HashMap<>();

        for (int i = startingRowNum+2; i <= endingRowNum; i++) {
            Row row = sheet.getRow(i);
            for (int j = 2; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                String header = sheet.getRow(startingRowNum + 1).getCell(j).getStringCellValue();

                String headerName;
                if (header.contains("{") && header.contains("}")) {
                    String[] parts = header.split("\\{", 2);
                    headerName = parts[0];
                } else {
                    headerName = header;
                }

                if (cell != null) {
                    if (headerName.contains(".")) {
                        String[] nestedHeaders = headerName.split("\\.");
                        Map<String, Object> nestedJson = new HashMap<>();
                        Map<String, Object> currentLevel = nestedJson;
                        for (int k = 1; k < nestedHeaders.length-1; k++) {
                            String key = nestedHeaders[k];
                            currentLevel.put(key, new HashMap<>());
                            currentLevel = (Map<String, Object>) currentLevel.get(key);
                        }
                        currentLevel.put(nestedHeaders[nestedHeaders.length - 1], getCellValue(cell));
                        responseDataList.put(nestedHeaders[0], nestedJson);
                    } else {
                        responseDataList.put(headerName, getCellValue(cell));
                    }
                }
            }
        }

        return responseDataList;

    }

    private static String convertToResponseJsonString (Map<String,Object> jsonDataList) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String responseJson = null;
        try {
           responseJson = objectMapper.writeValueAsString(jsonDataList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return responseJson;
    }

    public String generateResponseJsonString(Path path, String className, String methodName) {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(Files.newInputStream(path));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Sheet responseSheet = workbook.getSheet("RequestsAndResponses");

        int[] rowNums = getFilteredRows(responseSheet,className,methodName);
        Map<String,Object> jsonDataList = getResponseDataList(responseSheet,rowNums[0],rowNums[1]);

        return convertToResponseJsonString(jsonDataList);
    }

    private boolean isRowEmpty(Row row) {
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private boolean isMatchingRow(Row row, String className, String methodName) {
        Cell classNameCell = row.getCell(0);
        Cell methodNameCell = row.getCell(1);

        if (classNameCell != null && methodNameCell != null) {
            String rowClassName = classNameCell.getStringCellValue();
            String rowMethodName = methodNameCell.getStringCellValue();
            return className.equals(rowClassName) && methodName.equals(rowMethodName);
        }
        return false;
    }

    private int[] getFilteredRows(Sheet sheet, String className, String methodName) {


        int startingRowNum = -1;
        int endingRowNum = -1;

        for (Row row : sheet) {
            if (isRowEmpty(row)) {
                continue;
            }
            if (isMatchingRow(row, className, methodName)) {
                if (startingRowNum == -1) {
                    startingRowNum = row.getRowNum(); // Set first row number
                }
                endingRowNum = row.getRowNum(); // Update last row number on each match
            }
        }

        return new int[]{startingRowNum, endingRowNum};

    }


}
