package com.sk.bds.datainsight.exception;


import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.response.ResponseCode;

public class ExceptionBase extends Exception{
    private ResponseBase responseBase;

    public ExceptionBase(ResponseCode responseCode, String message) {
        super(message);
        responseBase = new ResponseBase(responseCode);
    }

    public void setExceptionMessage(String exceptionMessage) {
        responseBase.setExceptionMessage(exceptionMessage);
    }

    public void setSubErrorCode(int code) {
        responseBase.setSubErrorCode(code);
    }

    public void setMessage(String message) {
        responseBase.setMessage(message);
    }

    public Object getRes() {
        return responseBase.getRes();
    }
}
