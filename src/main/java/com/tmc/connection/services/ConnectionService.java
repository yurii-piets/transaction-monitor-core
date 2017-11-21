package com.tmc.connection.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages the Connection to Databases
 *
 * @see Connection
 * @see DataSource
 */
@Service
public class ConnectionService {

    private final ConfigurableApplicationContext applicationContext;

    private final Map<String, Connection> cachedConnections = new HashMap<>();

    @Autowired
    public ConnectionService(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @return Set of Connection to all databases specified by qualifier in @DatabaseProperty
     *
     * if connection was already requested takes it from a cache
     * if not creates new instances of connection and out it into cache
     *
     * @see com.tmc.connection.annotation.DatabaseProperty
     * @see Connection
     */
    public Set<Connection> getAllConnections() {
        Map<String, DataSource> dataSourcesBeans = applicationContext.getBeansOfType(DataSource.class);
        Set<Connection> connectionsSet = new HashSet<>();

        for (String key : dataSourcesBeans.keySet()) {
            if (cachedConnections.containsKey(key)) {
                connectionsSet.add(cachedConnections.get(key));
            } else {
                cachedConnections.put(key, cachedConnections.get(key));
                connectionsSet.add(cachedConnections.get(key));
            }
        }

        return connectionsSet;
    }

    /**
     * If connection to qualified database was already requested takes it from cache
     * or else establish new connection with database
     *
     * @param qualifier to specify with wich database Connection is requested
     * @return instance of Connection
     * @throws SQLException if connection with database could not be established
     *
     * @see Connection
     * @see com.tmc.connection.annotation.DatabaseProperty
     */
    public Connection getConnectionByQualifier(String qualifier) throws SQLException {
        if (cachedConnections.containsKey(qualifier)) {
            Connection connection = cachedConnections.get(qualifier);
            return connection;
        }

        DataSource dataSource = applicationContext.getBean(qualifier, DataSource.class);
        if (dataSource == null) {
            throw new IllegalArgumentException("Database qualifier: [" + qualifier + "] does not exist");
        }


        Connection connection = dataSource.getConnection();
        cachedConnections.put(qualifier, connection);

        return connection;
    }

    /**
     * Empty cache of Connection
     */
    public void clearCache() {
        cachedConnections.clear();
    }
}
