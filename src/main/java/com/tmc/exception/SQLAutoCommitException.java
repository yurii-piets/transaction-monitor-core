package com.tmc.exception;

public class SQLAutoCommitException extends RuntimeException {

    private final static String message = "Auto-commit could not be turned-off";

    public SQLAutoCommitException(Throwable cause) {
        super(message, cause);
    }
}

