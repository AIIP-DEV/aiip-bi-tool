package com.sk.bds.datainsight.response;

import java.util.HashMap;

public class ResponseBase {
    private int code;
    private String message;
    private String exceptionMessage;
    private Integer subErrorCode;
    private HashMap<String, Object> data = new HashMap<>();

    public ResponseBase() {
        code = ResponseCode.SUCCESS.getValue();
        message = ResponseCode.SUCCESS.toString();
    }

    public ResponseBase(ResponseCode responseCode) {
        code = responseCode.getValue();
        message = responseCode.toString();
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public void setSubErrorCode(int subErrorCode) {
        this.subErrorCode = subErrorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HashMap<String, Object> getRes() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("statusCode", code);
        if (message != null) {
            result.put("message", message);
        }
        if (exceptionMessage != null) {
            result.put("exceptionMessage", exceptionMessage);
        }
        if (subErrorCode != null) {
            result.put("subErrorCode", subErrorCode);
        }
        if (data.size() > 0) {
            result.put("data", data);
        }
        return result;
    }

    public HashMap<String, Object> getRes(Object resultData) {
        HashMap<String, Object> result = getRes();
        result.put("data", resultData);
        return result;
    }
}
