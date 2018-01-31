package com.tmc.exception;

import java.sql.SQLException;

/**
 * An exception that provides information on a database reverting query error.
 */
public class SQLRevertException extends SQLException {

    private final static String message = "Revert cannot be applied on database";

    public SQLRevertException(SQLException cause) {
        super(message, cause);
    }
}
