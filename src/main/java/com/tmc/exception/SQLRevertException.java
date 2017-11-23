package com.tmc.exception;

public class SQLRevertException extends Exception {

    private final static String message = "Revert cannot be applied on database";

    public SQLRevertException(Throwable cause) {
        super(message, cause);
    }
}