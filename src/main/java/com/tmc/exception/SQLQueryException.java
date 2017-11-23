package com.tmc.exception;

public class SQLQueryException extends Exception {

    private final static String message = "SQL query cannot be executed on database";

    public SQLQueryException(Throwable cause) {
        super(message, cause);
    }
}