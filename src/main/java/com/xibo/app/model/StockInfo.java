package com.xibo.app.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by wangx on 14/10/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockInfo implements Serializable {
    private int accumAdjFactor, dealAmount;
    private long marketValue, negMarketValue;
    private double actPreClosePrice, closePrice, highestPrice, lowestPrice, openPrice, preClosePrice, turnoverRate;
    private String exchangeCD, secID, secShortName, ticker, tradeDate;

    public boolean isLimitUp() {
        if (actPreClosePrice == 0) return false;
        if (secShortName.contains("ST")) {
            return closePrice / actPreClosePrice > 1.049;
        }
        return closePrice / actPreClosePrice > 1.099;
    }

    public long getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(long marketValue) {
        this.marketValue = marketValue;
    }

    public long getNegMarketValue() {
        return negMarketValue;
    }

    public void setNegMarketValue(long negMarketValue) {
        this.negMarketValue = negMarketValue;
    }

    public int getAccumAdjFactor() {

        return accumAdjFactor;
    }

    public void setAccumAdjFactor(int accumAdjFactor) {
        this.accumAdjFactor = accumAdjFactor;
    }

    public int getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(int dealAmount) {
        this.dealAmount = dealAmount;
    }

    public double getActPreClosePrice() {
        return actPreClosePrice;
    }

    public void setActPreClosePrice(double actPreClosePrice) {
        this.actPreClosePrice = actPreClosePrice;
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

    public double getTurnoverRate() {
        return turnoverRate;
    }

    public void setTurnoverRate(double turnoverRate) {
        this.turnoverRate = turnoverRate;
    }

    public String getExchangeCD() {
        return exchangeCD;
    }

    public void setExchangeCD(String exchangeCD) {
        this.exchangeCD = exchangeCD;
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
        this.tradeDate = tradeDate;
    }

    @Override
    public String toString() {
        double rate = closePrice / actPreClosePrice -1;
        return String.format("StockId: %s ShortName: %s Rate %.2f%%. \n", secID, secShortName, rate * 100);
    }
}
