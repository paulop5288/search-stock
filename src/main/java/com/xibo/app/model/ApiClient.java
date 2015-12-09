package com.xibo.app.model;

import com.xibo.app.controller.RootController;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangx on 09/10/2015.
 */
public class ApiClient {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(500);
    private final String domain = "https://api.wmcloud.com:443/data/v1";
    //private final String properties = "src/resources/config.properties";
    private final String authCodeKey = "appKey";
    private Properties properties;
    private String appKey;
    private final RootController delegate;
    private DataReader reader = new DataReader();
    private int counter;
    private Object lock = new Object();

    public ApiClient(RootController delegate) {
        this.delegate = delegate;
        properties = new Properties();
        appKey = "ce2f2460f3fe70dc19d92e2722894d37102cca81ff5728bdd1979d8a3eadd3e7";
        //readConfigFrom(getClass().getResource("/config.properties").getFile());
    }

    public void readDataFromAPI(String endPoint, Integer timeout, CallbackHandler handler) {
        HttpGet httpGet = new HttpGet(domain + endPoint);
        httpGet.addHeader("Authorization", "Bearer " + appKey);
        threadPool.submit(() -> {
            try {
                handler.callback(sendHttpRequest(httpGet, timeout));
            } catch (ConnectTimeoutException eTimeout) {
                String error = "读取HTTP: " + httpGet + " 超时 ";
                if (delegate != null) delegate.log(error);
                System.err.println(error);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void readDataFromAPI(String endPoint, Integer timeout, Integer times, CallbackHandler handler) {
        HttpGet httpGet = new HttpGet(domain + endPoint);
        httpGet.addHeader("Authorization", "Bearer " + appKey);
        threadPool.submit(() -> handler.callback(readDataFromAPIBlocking(endPoint, timeout, times)));
    }


    public String readDataFromAPIBlocking(String endPoint, Integer timeout) {
        try {
            HttpGet httpGet = new HttpGet(domain + endPoint);
            httpGet.addHeader("Authorization", "Bearer " + appKey);
            return sendHttpRequest(httpGet, timeout);
        } catch (Exception e) {
            delegate.log("无法获取 :" + endPoint + "  尝试再次获取");
        }
        return null;
    }

    public String readDataFromAPIBlocking(String endPoint, Integer timeout, Integer times) {
        String s = null;
        while (times > 0) {
            s = readDataFromAPIBlocking(endPoint, timeout);
            if (s != null) {
                return s;
            }
            times--;
        }
        delegate.log("尝试" + times + "次失败");
        return s;
    }

    private AtomicBoolean finished = new AtomicBoolean();
    public void readMultiDataFromAPI(List<StockInfo> stocks) {
        counter = 0;
        finished.set(false);
        for (StockInfo stock : stocks) {
            threadPool.submit(() -> {
                delegate.log(String.format("正在获取 %s 数据 ", stock.getSecShortName()));

                if (!delegate.getCache().contains(stock.getTicker())) {
                    String jsonResult = readDataFromAPIBlocking(String.format("/api/market/getMktEqudJY.json?secID=%s&startDate=19900101&endDate=20151208", stock.getSecID()), 10, 10);
                    if (jsonResult != null) {
                        List<StockInfo> result = reader.readStocksFrom(jsonResult);
                        delegate.getCache().store(result);
                        StockInfo sampleStock = result.get(0);
                        synchronized (lock) {
                            delegate.log(String.format(
                                    "获得 %s 历史数据: 共 %d 条, %d/%d",
                                    sampleStock.getSecShortName(),
                                    result.size(),
                                    ++counter,
                                    stocks.size()));
                        }
                    }
                } else {
                    synchronized (lock) {
                        delegate.log(String.format("已经获得 %s 历史数据: %d/%d",
                                stock.getSecShortName(),
                                ++counter,
                                stocks.size()));
                        delegate.setProgress(counter + "/" + stocks.size());
                    }
                }

                if (counter >= stocks.size()) {
                    delegate.log("获得全部数据");
                    finished.set(true);
                }
            });
        }
        while (!finished.get()) {

        }
    }

    public String sendHttpRequest(HttpUriRequest httpRequest, Integer timeout) throws IOException {
        HttpEntity entity;
        if (timeout != null) {
            timeout = timeout * 1000;
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeout)
                    .setSocketTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .build();

            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            HttpResponse response = httpClient.execute(httpRequest);
            entity = response.getEntity();
        } else {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(httpRequest);
            entity = response.getEntity();
        }
        return EntityUtils.toString(entity);
    }

    public void readConfigFrom(String locatoin) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(locatoin);
            if (stream != null) {
                properties.load(stream);
                appKey = properties.getProperty(authCodeKey);
            } else {
                // unable to load config properties
                System.out.println("Unable to load file :" + locatoin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {

            }
        }
    }

}
