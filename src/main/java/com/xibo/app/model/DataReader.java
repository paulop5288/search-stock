package com.xibo.app.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xibo.app.controller.RootController;
import com.xibo.app.lib.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by wangx on 13/10/2015.
 */
public class DataReader {

    public List<StockInfo> readStocksFrom(List<String> cache) {
        ObjectMapper mapper = new ObjectMapper();
        JSONObject rawData = new JSONObject(cache.get(0));
        RetCode retCode = extractRetCode(rawData);

        switch (retCode) {
            case SUCCESS:
                try {
                    return mapper.readValue(rawData.get("data").toString(), new TypeReference<List<StockInfo>>(){});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    public List<StockInfo> readStocksFrom(String json) {
        ObjectMapper mapper = new ObjectMapper();
        JSONObject rawData = new JSONObject(json);
        RetCode retCode = extractRetCode(rawData);

        switch (retCode) {
            case SUCCESS:
                try {
                    return mapper.readValue(rawData.get("data").toString(), new TypeReference<List<StockInfo>>(){});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    public RetCode extractRetCode(JSONObject rawData) {
        if (!rawData.isNull("retCode")) {
            int code = rawData.getInt("retCode");
            if (code == 1) {
                return RetCode.SUCCESS;
            } else {
                return RetCode.FAILURE;
            }
        }
        throw new RuntimeException("Cannot find information about market.");
    }

}
