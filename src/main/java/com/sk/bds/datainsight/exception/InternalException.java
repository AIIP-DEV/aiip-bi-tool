package com.sk.bds.datainsight.exception;


import com.sk.bds.datainsight.response.ResponseCode;

public class InternalException extends ExceptionBase{

    public InternalException(String message) {
        super(ResponseCode.INTERNAL_SERVER_ERROR, message);
        setMessage(message);
    }

    public InternalException(Exception e) {
        super(ResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
        setExceptionMessage(e.getMessage());
    }
}
