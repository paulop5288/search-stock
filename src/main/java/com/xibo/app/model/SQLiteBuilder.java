package com.xibo.app.model;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangx on 07/12/2015.
 */
public class SQLiteBuilder {
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);

    public SQLiteBuilder() {
    }

    public void initDatabase(CallbackHandler callback) {
        SQLiteClient client = new SQLiteClient();
        client.createTableForStockMarket();
        client.deleteAllRows();
        threadPool.submit(() -> {
            ApiClient apiClient = new ApiClient(null);
            String endPoint = "/api/market/getMktEqud.json?field=&tradeDate=20151204";
            System.out.println("obtaining data");
            String result = apiClient.readDataFromAPIBlocking(endPoint, null);
            DataReader reader = new DataReader();
            List<StockInfo> stockInfos = reader.readStocksFrom(result);
            for (StockInfo stock : stockInfos) {
                client.insertValues(stock.getTicker(), stock.getSecID(), stock.getSecShortName());
            }
            System.out.println("get existing stocks");
            callback.callback(null);
        });
    }

    public void addStocksOf(List<StockInfo> stocks) {
        SQLiteClient client = new SQLiteClient();
        AtomicInteger i = new AtomicInteger(0);
        stocks = stocks.subList(0, 100);
        for (StockInfo stock: stocks) {
            threadPool.submit(() -> {
                ApiClient apiClient = new ApiClient(null);
                String endPoint = "/api/market/getMktEqudJY.json?secID=" + stock.getSecID() + "&startDate=19900101&endDate=20151207";
                System.out.println("submitted for fetching " + stock.getSecID());
                String result = apiClient.readDataFromAPIBlocking(endPoint, null);
                DataReader reader = new DataReader();
                List<StockInfo> stockInfos = reader.readStocksFrom(result);
                for (StockInfo newStock : stockInfos) {
                    client.insertValues(newStock);
                }
                System.out.println("inserted into database for " + stock.getSecID());
                int last =  i.incrementAndGet();
                if (last == 100) {
                    System.out.println("done getting 100 stocks");
                }
            });
        }
    }
}
