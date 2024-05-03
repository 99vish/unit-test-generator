package com.blumeglobal.tests.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExcelUtil {

    private static boolean isRowEmpty(Row row) {
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMatchingRow(Row row, String className, String methodName) {
        Cell classNameCell = row.getCell(0);
        Cell methodNameCell = row.getCell(1);

        if (classNameCell != null && methodNameCell != null) {
            String rowClassName = classNameCell.getStringCellValue();
            String rowMethodName = methodNameCell.getStringCellValue();
            return className.equals(rowClassName) && methodName.equals(rowMethodName);
        }
        return false;
    }

    public static int[] getFilteredRows(Sheet sheet, String className, String methodName) {


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

    public static Sheet getSheetFromWorkbook(Path path, String name){
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(Files.newInputStream(path));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return workbook.getSheet(name);
    }
}
