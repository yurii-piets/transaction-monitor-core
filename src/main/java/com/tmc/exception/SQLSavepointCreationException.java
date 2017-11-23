package com.tmc.exception;

public class SQLSavepointCreationException extends Exception {

    private final static String message = "Savepoint cannot be created on this connection";

    public SQLSavepointCreationException(Throwable cause) {
        super(message, cause);
    }
}
