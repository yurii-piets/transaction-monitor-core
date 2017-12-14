package com.tmc.exception;

public class SQLStatementException extends Exception {

    private final static String message = "Statement cannot be retried from current connection";

    public SQLStatementException(Throwable cause) {
        super(message, cause);
    }
}
