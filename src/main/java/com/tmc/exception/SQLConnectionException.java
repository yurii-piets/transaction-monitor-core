package com.tmc.exception;

import java.sql.SQLException;

public class SQLConnectionException extends SQLException {

    private final static String message = "Connection with database cannot be established";

    public SQLConnectionException(SQLException cause) {
        super(message, cause);
    }
}
