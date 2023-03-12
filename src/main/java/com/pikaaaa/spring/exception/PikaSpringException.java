package com.pikaaaa.spring.exception;

public class PikaSpringException extends RuntimeException{

    private String msg;

    public PikaSpringException() {
        super();
    }

    public PikaSpringException(String message) {
        super(message);
        this.msg = message;
    }

    public PikaSpringException(String message, Throwable cause) {
        super(message, cause);
    }

    public PikaSpringException(Throwable cause) {
        super(cause);
    }

    protected PikaSpringException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getMsg() {
        return msg;
    }
}
