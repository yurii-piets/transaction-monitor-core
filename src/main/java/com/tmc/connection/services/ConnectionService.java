package com.tmc.connection.services;

import com.tmc.connection.pool.def.ConnectionPool;
import com.tmc.exception.SQLConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the Connection to Databases
 *
 * @see Connection
 * @see DataSource
 */
public class ConnectionService {

    private final ConnectionPool connectionPool;

    private final Map<String, Connection> cachedConnections = new HashMap<>();

    public ConnectionService(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * If connection to qualified database was already requested takes it from cache
     * or else establish new connection with database
     *
     * @param qualifier to specify with wich database Connection is requested
     * @return instance of Connection
     * @see Connection
     * @see com.tmc.connection.annotation.DatabaseProperty
     */
    public Connection getConnectionByQualifier(String qualifier) throws SQLConnectionException {
        if (cachedConnections.containsKey(qualifier)) {
            return cachedConnections.get(qualifier);
        }

        Connection connection;
        connection = connectionPool.acquire(qualifier);
        cachedConnections.put(qualifier, connection);

        return connection;
    }

    /**
     * Release all cached connections back to pool
     * @see Connection
     */
    public void releaseConnections() {
        for (Connection connection : cachedConnections.values()) {
            connectionPool.release(connection);
        }

        cachedConnections.clear();
    }
}
