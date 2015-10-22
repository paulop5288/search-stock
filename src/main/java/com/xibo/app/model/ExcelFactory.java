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
    
    public void createExcelFrom(List<List<StockInfo>> contents, int currentIndex) {
        workbook = new XSSFWorkbook();
        XSSFSheet workSheet = workbook.createSheet("分析");
        int rowIndex = 0;
        XSSFRow firstRow = workSheet.createRow(rowIndex++);
        AddHeadersToRow(firstRow, contents, currentIndex);
        for (List<StockInfo> stocks : contents) {
            if (stocks.size() == 0) continue;
            XSSFRow row = workSheet.createRow(rowIndex++);
            int cellIndex = 0;
            row.createCell(cellIndex++).setCellValue(stocks.get(currentIndex).getTradeDate());
            row.createCell(cellIndex++).setCellValue(stocks.get(currentIndex).getSecID());
            row.createCell(cellIndex++).setCellValue(stocks.get(currentIndex).getSecShortName());
            row.createCell(cellIndex++).setCellValue(stocks.get(currentIndex).getClosePrice());
            row.createCell(cellIndex++).setCellValue(stocks.get(currentIndex).getOpenPrice());
            row.createCell(cellIndex++).setCellValue(stocks.get(currentIndex).getLowestPrice());
            row.createCell(cellIndex++).setCellValue(stocks.get(currentIndex).getHighestPrice());
            for (StockInfo stock : stocks) {
                if (!stock.equals(stocks.get(currentIndex))) {
                    if (stock == null) {
                        row.createCell(cellIndex++).setCellValue("不存在");
                        row.createCell(cellIndex++).setCellValue("不存在");
                        row.createCell(cellIndex++).setCellValue("不存在");
                        row.createCell(cellIndex++).setCellValue("不存在");
                    } else {
                        row.createCell(cellIndex++).setCellValue(stock.getClosePrice());
                        row.createCell(cellIndex++).setCellValue(stock.getOpenPrice());
                        row.createCell(cellIndex++).setCellValue(stock.getLowestPrice());
                        row.createCell(cellIndex++).setCellValue(stock.getHighestPrice());
                    }
                }
            }
        }
    }
    private void AddHeadersToRow(XSSFRow row, List<List<StockInfo>> contents, int currentIndex) {
        int cellIndex = 0;
        row.createCell(cellIndex++).setCellValue("日期");
        row.createCell(cellIndex++).setCellValue("股票代码");
        row.createCell(cellIndex++).setCellValue("股票名称");
        row.createCell(cellIndex++).setCellValue("当日收盘价");
        row.createCell(cellIndex++).setCellValue("当日开盘价");
        row.createCell(cellIndex++).setCellValue("当日最低价");
        row.createCell(cellIndex++).setCellValue("当日最高价");
        for (int i = 0; i < contents.get(0).size(); i++) {
            if (i < currentIndex) {
                row.createCell(cellIndex++).setCellValue("前" + (currentIndex - i) +"日收盘价");
                row.createCell(cellIndex++).setCellValue("前" + (currentIndex - i) +"日开盘价");
                row.createCell(cellIndex++).setCellValue("前" + (currentIndex - i) +"日最低价");
                row.createCell(cellIndex++).setCellValue("前" + (currentIndex - i) +"日最高价");
            } else if (i > currentIndex) {
                row.createCell(cellIndex++).setCellValue("后" + (i - currentIndex) + "日收盘价");
                row.createCell(cellIndex++).setCellValue("后" + (i - currentIndex) + "日开盘价");
                row.createCell(cellIndex++).setCellValue("后" + (i - currentIndex) + "日最低价");
                row.createCell(cellIndex++).setCellValue("后" + (i - currentIndex) + "日最高价");
            }
        }
    }
    public void saveFileTo(String location, String name) {
        try {
            FileOutputStream out =
                    new FileOutputStream(new File(location + "/" + name));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
