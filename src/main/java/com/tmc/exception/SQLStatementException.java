package com.tmc.exception;

import java.sql.SQLException;

public class SQLStatementException extends SQLException {

    private final static String message = "Statement cannot be retried from current connection";

    public SQLStatementException(SQLException cause) {
        super(message, cause);
    }
}
