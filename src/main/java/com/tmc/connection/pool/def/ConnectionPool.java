package com.tmc.connection.pool.def;

import com.tmc.exception.SQLConnectionException;

import java.sql.Connection;

/**
 * Pool that contains pool of connection with databases
 */
public interface ConnectionPool {

    /**
     * Acquires connections withs database from the pool
     *
     * @param qualifier - that specifies database
     * @return connection with database
     * @throws SQLConnectionException - if connection cannot be established with database specified by qualifier
     * @see Connection
     */
    Connection acquire(String qualifier) throws SQLConnectionException;

    /**
     * Release connection back to pool in case of any issue with connection,
     * connection will be closed
     * @param connection that is released back to pool
     */
    void release(Connection connection);
}
