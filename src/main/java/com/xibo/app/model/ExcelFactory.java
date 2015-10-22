package com.xibo.app.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.List;

/**
 * Created by wangx on 18/10/2015.
 */
public class ExcelFactory {
    private XSSFWorkbook workbook;
    public ExcelFactory() {
        
    }
    
    public void createExcelFrom(List<List<StockInfo>> contents) {
        workbook = new XSSFWorkbook();
        XSSFSheet workSheet = workbook.createSheet("分析");
        int rowIndex = 0;
        for (List<StockInfo> stocks : contents) {
            XSSFRow row = workSheet.createRow(rowIndex++);
            int cellIndex = 0;
            row.createCell(cellIndex++).setCellValue(stocks.get(0).getTradeDate());
            row.createCell(cellIndex++).setCellValue(stocks.get(0).getSecID());
            row.createCell(cellIndex++).setCellValue(stocks.get(0).getSecShortName());
            for (StockInfo stock : stocks) {
                double rate = stock.getClosePrice() / stock.getActPreClosePrice() - 1;
                row.createCell(cellIndex++).setCellValue(rate * 100 + "%");
            }
        }
        XSSFRow row =workSheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("good");

        try {
            FileOutputStream out =
                    new FileOutputStream(new File("new.xls"));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
