package com.tmc.exception;

import java.sql.SQLException;

public class SQLSavepointCreationException extends SQLException {

    private final static String message = "Savepoint cannot be created on this connection";

    public SQLSavepointCreationException(SQLException cause) {
        super(message, cause);
    }
}
