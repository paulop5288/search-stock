package com.xibo.app.model;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wangx on 19/10/2015.
 */
public class InMemoryCache implements Serializable {
    private Map<String, List<StockInfo>> cache = new HashMap<>();

    public void store(List<StockInfo> stocks) {
        List<StockInfo> filteredStocks = stocks.stream()
                .filter(s -> s.getDealAmount() != 0)
                .collect(Collectors.toList());
        cache.put(stocks.get(0).getTicker(), filteredStocks);
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

    public List<StockInfo> findStockInfoBtw(StockInfo stock, int previousDays, int followingDays) {
        LinkedList<StockInfo> selected = new LinkedList<>();
        int beginIndex = 0;
        List<StockInfo> stocks = getStocks(stock.getTicker());
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getTradeDate().equals(stock.getTradeDate())) {
                beginIndex = i;
                break;
            }
        }

        if (beginIndex - previousDays >= 0 && beginIndex + followingDays < stocks.size()) {
            for (int i = beginIndex - previousDays;i <= beginIndex + followingDays; i ++) {
                selected.add(stocks.get(i));
            }
        }
        return selected;
    }

}
