package com.tmc.exception;

public class SQLAutoCommitException extends RuntimeException {

    public SQLAutoCommitException(String message) {
        super(message);
    }
}

