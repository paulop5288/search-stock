package com.xibo.app.model;

import com.xibo.app.controller.RootController;
import javafx.application.Platform;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangx on 09/10/2015.
 */
public class Client {
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final String domain = "https://api.wmcloud.com:443/data/v1";
    //private final String properties = "src/resources/config.properties";
    private final String authCodeKey = "appKey";
    private Properties properties;
    private String appKey;
    private final RootController delegate;
    private DataReader reader = new DataReader();
    private AtomicInteger index = new AtomicInteger();

    public Client(RootController delegate) {
        this.delegate = delegate;
        properties = new Properties();
        appKey = "ce2f2460f3fe70dc19d92e2722894d37102cca81ff5728bdd1979d8a3eadd3e7";
        //readConfigFrom(getClass().getResource("/config.properties").getFile());
    }

    public void readDataFromAPI(String endPoint, CallbackHandler handler) {
        HttpGet httpGet = new HttpGet(domain + endPoint);
        httpGet.addHeader("Authorization", "Bearer " + appKey);
        threadPool.submit(()-> {
            try {
                handler.callback(sendHttpRequest(httpGet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public String readDataFromAPIBlocking(String endPoint) {
        try {
            HttpGet httpGet = new HttpGet(domain + endPoint);
            httpGet.addHeader("Authorization", "Bearer " + appKey);
            return sendHttpRequest(httpGet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void readMultiDataFromAPI(List<StockInfo> stocks) {
        index.set(1);
        for (StockInfo stock : stocks) {
            threadPool.submit(()-> {
                Platform.runLater(() -> delegate.log(String.format("正在获取 %s 数据 ", stock.getSecShortName())));

                if (!delegate.getCache().contains(stock.getTicker())) {
                    String jsonResult = readDataFromAPIBlocking(String.format("/api/market/getMktEqud.json?field=&ticker=%s", stock.getTicker()));
                    if (jsonResult != null) {
                        List<StockInfo> result = reader.readStocksFrom(jsonResult);
                        delegate.getCache().store(result);
                        StockInfo sampleStock = result.get(0);
                        Platform.runLater(() -> delegate.log(String.format(
                                "获得 %s 历史数据: 共 %d 条, %d/%d", sampleStock.getSecShortName(), result.size(), index.getAndIncrement(), stocks.size())));
                    }
                } else {
                    Platform.runLater(() -> delegate.log(String.format("已经获得 %s 历史数据: %d/%d", stock.getSecShortName(), index.getAndIncrement(), stocks.size())));
                }

                if (index.get() >= stocks.size()) {
                    System.out.println(index.get());
                    Platform.runLater(() -> {
                        delegate.log("获得全部数据");
                        delegate.loadingProgressBar.setProgress(1d);
                        delegate.confirmButton.setDisable(false);
                    });
                }
            });
        }
    }

    public String sendHttpRequest(HttpUriRequest httpRequest) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpRequest);
        HttpEntity entity = response.getEntity();
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
