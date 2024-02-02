package com.blumeglobal.tests.util;

import com.blumeglobal.tests.model.excel.InputTestCases;
import com.poiji.bind.Poiji;
import com.poiji.option.PoijiOptions;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.util.List;

public class FileReaderUtil {

    public static <T> List<T> readExcelFile(String filePath, Sheet sheet, Class<T>type) {
        File file = new File(filePath);
        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
                .trimCellValue(true)
                .ignoreWhitespaces(true)
                .build();
        return Poiji.fromExcel(sheet, type, options);
    }
}
