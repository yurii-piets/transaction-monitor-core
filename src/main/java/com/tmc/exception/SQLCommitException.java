package com.tmc.exception;

public class SQLCommitException extends RuntimeException {

    public SQLCommitException (String message) {
        super(message);
    }
}
