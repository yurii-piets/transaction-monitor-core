package com.tmc.exception;

public class SQLConnectionException extends Exception {

    private final static String message = "Connection with database cannot be established";

    public SQLConnectionException(Throwable cause) {
        super(message, cause);
    }
}