package com.sk.bds.datainsight.exception;


import com.sk.bds.datainsight.response.ResponseCode;

public class ForbiddenException extends ExceptionBase{
    public ForbiddenException(String message) {
        super(ResponseCode.FORBIDDEN_ERROR, message);
        setMessage(message);
    }
}
