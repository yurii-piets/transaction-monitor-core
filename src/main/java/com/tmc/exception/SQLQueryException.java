package com.tmc.exception;

import java.sql.SQLException;

public class SQLQueryException extends SQLException {

    private final static String message = "SQL query cannot be executed on database";

    public SQLQueryException(SQLException cause, String sql) {
        super(message + ": " + sql, cause);
    }
}
