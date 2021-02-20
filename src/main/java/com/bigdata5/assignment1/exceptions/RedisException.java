package com.bigdata5.assignment1.exceptions;

public class RedisException extends RuntimeException {

    private String errCode;

    private static final long serialVersionUID = 1L;

    public RedisException(String errorMessage, String errCode) {
        super(errorMessage);
        this.errCode = errCode;
    }

    public RedisException(String errorMessage, Throwable err, String errCode) {
        super(errorMessage, err);
        this.errCode = errCode;
    }

    public RedisException() {
        super();
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}
