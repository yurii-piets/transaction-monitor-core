package com.tmc.exception;

public class SQLSavepointCreationException extends RuntimeException {

    private final static String message = "Savepoint cannot be created on this connection";

    public SQLSavepointCreationException(Throwable cause) {
        super(message, cause);
    }
}
