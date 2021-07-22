package com.sk.bds.datainsight.response;


public enum ResponseCode {
    SUCCESS(200),
    CREATED(201),
    BAD_REQUEST_ERROR(400),
    AUTH_ERROR(401),
    FORBIDDEN_ERROR(403),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    private int value;

    ResponseCode(int val) {
        this.value = val;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        switch (this) {
            case SUCCESS: {
                return "OK";
            }
            case CREATED: {
                return "Created";
            }
            case BAD_REQUEST_ERROR: {
                return "Bad Request";
            }
            case AUTH_ERROR: {
                return "Unauthorized";
            }
            case FORBIDDEN_ERROR: {
                return "Forbidden";
            }
            case NOT_FOUND: {
                return "Not found";
            }
            case INTERNAL_SERVER_ERROR: {
                return "Internal server error";
            }
            default:
                return "Unknown error";
        }
    }
}
