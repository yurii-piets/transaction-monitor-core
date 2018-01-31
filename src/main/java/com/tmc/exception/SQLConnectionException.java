package com.tmc.exception;

import java.sql.SQLException;

/**
 * An exception that provides information on a database connection error.
 */
public class SQLConnectionException extends SQLException {

    private final static String message = "Connection with database cannot be established";

    public SQLConnectionException(SQLException cause) {
        super(message, cause);
    }
}
