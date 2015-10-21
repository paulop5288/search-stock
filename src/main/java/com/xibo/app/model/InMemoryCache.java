package com.xibo.app.model;

import java.io.Serializable;
import java.util.*;

/**
 * Created by wangx on 19/10/2015.
 */
public class InMemoryCache implements Serializable {
    private Map<String, ArrayList<StockInfo>> cache = new HashMap<>();

    public void store(List<StockInfo> stocks) {
        cache.put(stocks.get(0).getTicker(), new ArrayList<>(stocks));
    }

    public List<StockInfo> getStocks(String ticker) {
        return cache.get(ticker);
    }

    public String getLastestDateFor(StockInfo stock) {
        return getLastestDateFor(stock.getTicker());
    }

    public String getLastestDateFor(String ticker) {
        List<StockInfo> stocks = getStocks(ticker);
        if (stocks == null) return null;
        return stocks.get(stocks.size() - 1).getTradeDate();
    }

    public boolean contains(String ticker) {
        List<StockInfo> data = cache.get(ticker);
        return data != null;
    }

}
