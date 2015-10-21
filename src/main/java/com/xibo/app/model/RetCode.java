package com.xibo.app.model;

/**
 * Created by wangx on 13/10/2015.
 */
public enum RetCode {
        SUCCESS(1), FAILURE(-1);

        private int code;
        private RetCode(int code) {
            this.code = code;
        }
}
