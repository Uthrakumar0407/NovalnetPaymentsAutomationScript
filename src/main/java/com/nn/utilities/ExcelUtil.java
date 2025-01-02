package com.nn.utilities;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class ExcelUtil {

    public static List<Map<String,String>> getData(String filePath, String sheetName){
        FileInputStream inputStream;
        XSSFWorkbook workbook;
        Sheet sheet;
        List<Map<String,String>> data = null;
        try{
            File file = new File(filePath);
            if(!file.exists()){
                throw new FileNotFoundException("Excel file not found at this path: "+filePath);
            }
            inputStream = new FileInputStream(file);
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheet(sheetName);

            Objects.requireNonNull(sheet,String.format("Sheet name %s is not found",sheetName));

            int rows = sheet.getLastRowNum();
            Log.info("Number of available rows: "+rows);
            int cols = sheet.getRow(0).getLastCellNum();
            Log.info("Number of available columns: "+cols);

            data = new ArrayList<>();

            Map<String,String> rowData;

            for(int row = 1; row <= rows; row++){
                rowData = new LinkedHashMap<>();
                for (int col = 0; col < cols; col++) {
                    rowData.put(formatCell(sheet.getRow(0).getCell(col)), formatCell(sheet.getRow(row).getCell(col)));
                }
                data.add(rowData);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    private static String formatCell(Cell cell) {
        DataFormatter format = new DataFormatter();
        return format.formatCellValue(cell);
    }

    public static void main(String[] args) {
        getData(System.getProperty("user.dir") + "/src/test/resources/TestDataNova.xlsx","Sample").forEach(System.out::println);
    }
}
