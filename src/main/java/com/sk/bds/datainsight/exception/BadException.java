package com.sk.bds.datainsight.exception;


import com.sk.bds.datainsight.response.ResponseCode;

public class BadException extends ExceptionBase{
    public BadException(String message) {
        super(ResponseCode.BAD_REQUEST_ERROR, message);
        setMessage(message);
    }
}
