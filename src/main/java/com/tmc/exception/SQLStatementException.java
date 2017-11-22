package com.tmc.exception;

public class SQLStatementException extends RuntimeException {

    private final static String message = "Statement connot be retrived from current connection";

    public SQLStatementException(Throwable cause) {
        super(message, cause);
    }
}
