package com.tmc.exception;

import java.sql.SQLException;

/**
 * An exception that provides information on a database commit error.
 */
public class SQLAutoCommitException extends SQLException {

    private final static String message = "Auto-commit could not be turned-off";

    public SQLAutoCommitException(SQLException cause) {
        super(message, cause);
    }
}

