package com.tmc.connection.services;

import com.tmc.ApplicationContext;
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

    private final ApplicationContext applicationContext;

    private final Map<String, Connection> cachedConnections = new HashMap<>();

    public ConnectionService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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

        DataSource dataSource = applicationContext.getDataSourceByQualifier(qualifier);
        if (dataSource == null) {
            throw new IllegalArgumentException("Database qualifier: [" + qualifier + "] does not exist");
        }

        Connection connection;
        try {
            connection = dataSource.getConnection();
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
        cachedConnections.clear();
    }
}
