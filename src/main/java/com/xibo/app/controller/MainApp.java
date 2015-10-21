package com.xibo.app.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/StockSearchApp.fxml"));
        primaryStage.setTitle("股票分析");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
//        String json = "{\"PB\": 1.0649999999999999, \"PE\": 7.5387000000000004, \"PE1\": 6.9351000000000003, \"accumAdjFactor\": 1, \"actPreClosePrice\": 10.9, \"closePrice\": 11.23, \"dealAmount\": 34196, \"exchangeCD\": \"XSHE\", \"highestPrice\": 11.380000000000001, \"lowestPrice\": 10.91, \"marketValue\": 160686433040.97, \"negMarketValue\": 132559532922.17, \"openPrice\": 10.960000000000001, \"preClosePrice\": 10.9, \"secID\": \"000001.XSHE\", \"secShortName\": \"\\u5e73\\u5b89\\u94f6\\u884c\", \"ticker\": \"000001\", \"tradeDate\": \"2015-10-12\", \"turnoverRate\": 0.0071999999999999998, \"turnoverValue\": 950602307.66999996, \"turnoverVol\": 84966515}";
//        ObjectMapper mapper = new ObjectMapper();
//          try {
//            mapper.readValue(json, StockInfo.class);
//        } catch (JsonMappingException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
    }
}
