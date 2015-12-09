package com.xibo.app.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by wangx on 14/10/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockInfo implements Serializable {
    private int dealAmount;
    private double closePrice, highestPrice, lowestPrice, openPrice, preClosePrice;
    private String secID, secShortName, ticker, tradeDate;

    public StockInfo(String ticker, String tradeDate, String secID, String secShortName, double closePrice, double highestPrice, double lowestPrice, double openPrice, double preClosePrice, int dealAmount) {
        this.tradeDate = tradeDate;
        this.closePrice = closePrice;
        this.highestPrice = highestPrice;
        this.lowestPrice = lowestPrice;
        this.openPrice = openPrice;
        this.preClosePrice = preClosePrice;
        this.secID = secID;
        this.secShortName = secShortName;
        this.ticker = ticker;
        this.dealAmount = dealAmount;
    }

    public StockInfo() {

    }

    public boolean isLimitUp() {
        if (!isInTrading()) return false;
        if (secShortName.contains("ST")) {
            return closePrice / openPrice > 1.049;
        }
        return closePrice / openPrice > 1.099;
    }

    public boolean isInTrading() {
        if (openPrice == 0) return false;
        return true;
    }

    public int getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(int dealAmount) {
        this.dealAmount = dealAmount;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(double highestPrice) {
        this.highestPrice = highestPrice;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getPreClosePrice() {
        return preClosePrice;
    }

    public void setPreClosePrice(double preClosePrice) {
        this.preClosePrice = preClosePrice;
    }

    public String getSecID() {
        return secID;
    }

    public void setSecID(String secID) {
        this.secID = secID;
    }

    public String getSecShortName() {
        return secShortName;
    }

    public void setSecShortName(String secShortName) {
        this.secShortName = secShortName;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {

        this.tradeDate = tradeDate.substring(0,10);
    }

    @Override
    public String toString() {
        double rate = closePrice / openPrice - 1;
        return String.format("StockId: %s ShortName: %s Date: %s Rate: %.2f%% DealAmount: %d. \n", secID, secShortName, tradeDate, rate * 100, dealAmount);
    }

}
