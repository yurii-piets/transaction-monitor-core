package com.tmc.connection.pool.def;

import com.tmc.exception.SQLConnectionException;

import java.sql.Connection;

public interface ConnectionPool {

    Connection acquire(String qualifier) throws SQLConnectionException;

    void release(Connection connection);
}
