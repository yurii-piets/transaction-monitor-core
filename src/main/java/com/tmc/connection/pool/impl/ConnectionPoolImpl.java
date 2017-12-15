package com.tmc.connection.pool.impl;

import com.tmc.connection.pool.def.ConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPoolImpl implements ConnectionPool {

    private final static int DEAFULT_SIZE = 10;

    private final Map<String, DataSource> dataSources;

    private final Map<String, Queue<Connection>> pool = new ConcurrentSkipListMap<>();

    private final Map<String, Queue<Connection>> available = new ConcurrentSkipListMap<>();

    private final Map<String, Queue<Connection>> acquired = new ConcurrentSkipListMap<>();

    public ConnectionPoolImpl(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public Connection acquire(String qualifier) throws SQLException {
        if (qualifier == null) {
            throw new IllegalArgumentException("Database qualifier cannot be null.");
        }

        Connection connection = createConnectionByQualifier(qualifier);

        return connection;
    }

    @Override
    public void release(Connection connection) {
        String qualifier = getQualifierByConnection(connection);

        if (qualifier == null) {
            return;
        }

        removeAcquired(qualifier, connection);
        addAvailableConnection(connection, qualifier);
    }

    private Connection createConnectionByQualifier(String qualifier) throws SQLException {
        if (!dataSources.containsKey(qualifier)) {
            throw new IllegalArgumentException("No database source can be accessed via qualifier \"" + qualifier + "\"");
        }

        DataSource dataSource = dataSources.get(qualifier);
        if(dataSource == null){
            throw new IllegalArgumentException("No database source can be accessed via qualifier \"" + qualifier + "\"");
        }

        Connection connection = dataSource.getConnection();
        return connection;
    }

    private String getQualifierByConnection(Connection connection) {
        for (Entry<String, Queue<Connection>> connections : pool.entrySet()) {
            if (connections.getValue().contains(connection)) {
                return connections.getKey();
            }
        }

        return null;
    }

    private void removeAcquired(String qualifier, Connection connection) {
        Queue<Connection> acquiredConnections = acquired.get(qualifier);
        acquiredConnections.remove(connection);
    }

    private void addAvailableConnection(Connection connection, String qualifier) {
        Queue<Connection> availableConnections = available.get(qualifier);
        if (availableConnections == null) {
            availableConnections = new LinkedBlockingQueue<>();
            available.put(qualifier, availableConnections);
        }

        availableConnections.add(connection);
    }

}
