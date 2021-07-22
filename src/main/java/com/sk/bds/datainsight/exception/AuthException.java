package com.sk.bds.datainsight.exception;


import com.sk.bds.datainsight.response.ResponseCode;

public class AuthException extends ExceptionBase{
    public AuthException(String message) {
        super(ResponseCode.AUTH_ERROR, message);
        setMessage(message);
    }
}
