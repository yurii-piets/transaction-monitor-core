package com.tmc.exception;

import java.sql.SQLException;

/**
 * /**
 * An exception that provides information on a database savepoint creation error.
 */
public class SQLSavepointCreationException extends SQLException {

    private final static String message = "Savepoint cannot be created on this connection";

    public SQLSavepointCreationException(SQLException cause) {
        super(message, cause);
    }
}
