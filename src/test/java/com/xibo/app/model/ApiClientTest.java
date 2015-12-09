package com.xibo.app.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by wangx on 24/11/2015.
 */
public class ApiClientTest {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private ApiClient apiClient;
    private AtomicInteger counter;

    @BeforeMethod
    public void setUp() {
        apiClient = new ApiClient(null);
        counter = new AtomicInteger(0);
    }

    @Test
    public void testTime() {
        String a = "2015-11-20 00:00:00";
        String b = "2015-11-20 00:00:00";
        System.out.println(a.compareTo(b));

    }

    @Test
    public void findNumberOfStocks() throws Exception {
        String endPoint = "/api/market/getMktEqud.json?field=&tradeDate=20151204";
        String result = apiClient.readDataFromAPIBlocking(endPoint, null);
        DataReader reader = new DataReader();
        List<StockInfo> stockInfos = reader.readStocksFrom(result);
        System.out.println("timeStopParsing" + LocalTime.now());
    }

    @Test
    public void testReadDataFromAPI() throws Exception {
        //000001.XSHE
        String endPoint = "/api/market/getMktEqudJY.json?secID=000001.XSHE&startDate=20151125&endDate=20151125";

        String result = apiClient.readDataFromAPIBlocking(endPoint, null);
        System.out.println(result);
        LocalTime now = LocalTime.now();

        DataReader reader = new DataReader();
        List<StockInfo> stockInfos = reader.readStocksFrom(result);
        System.out.println("timeStartParsing: " + now);
        System.out.println("timeStopParsing" + LocalTime.now());
        System.out.println("timeStartSearching" + LocalTime.now());
        System.out.println(stockInfos.get(0).getTradeDate());
        System.out.println("timeStopSearching" + LocalTime.now());
    }

    @Test
    public void getAllStocks() {
        AtomicInteger count = new AtomicInteger();
        String singlePoint = "/api/market/getMktEqudJY.json?secID=000001.XSHE&startDate=20150303&endDate=20150303";
        for (int i = 0;i < 50 ; i++) {
            threadPool.submit(() -> {
                System.out.println("submitted");
                System.out.println(apiClient.readDataFromAPIBlocking(singlePoint, null));
                count.incrementAndGet();
            });
        }
        while (count.get() < 5) {

        }
        System.out.println("Finished.");

    }

    public StockInfo findStock(String date) {
        return null;
    }
}