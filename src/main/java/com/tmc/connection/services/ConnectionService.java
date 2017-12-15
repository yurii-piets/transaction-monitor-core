package com.tmc.connection.services;

import com.tmc.connection.pool.def.ConnectionPool;
import com.tmc.exception.SQLConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
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
            Connection connection = cachedConnections.get(qualifier);
            return connection;
        }

        Connection connection;
        try {
            connection = connectionPool.acquire(qualifier);
        } catch (SQLException e) {
            throw new SQLConnectionException(e);
        }
        cachedConnections.put(qualifier, connection);

        return connection;
    }

    /**
     * Empties cache of Connection
     */
    public void clearCache() {
        for (Connection connection : cachedConnections.values()) {
            connectionPool.release(connection);
        }
    }
}
