package com.tmc.connection.pool.impl;

import com.tmc.connection.pool.def.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPoolImpl implements ConnectionPool {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final static int DEFAULT_SIZE = 10;

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

        Connection connection = null;

        Queue<Connection> availableQueue = available.get(qualifier);
        if (availableQueue == null) {
            connection = createConnectionByQualifier(qualifier);
        } else if (availableQueue.isEmpty()) {
            Queue<Connection> pollQueue = pool.get(qualifier);
            if (pollQueue.size() <= DEFAULT_SIZE) {
                connection = createConnectionByQualifier(qualifier);
            } else {
                throw new IllegalStateException("Pool is empty and not able to create new connection.");
//                try {
//                    while (availableQueue.isEmpty()) {
//                        wait();
//                    }
//                    connection = availableQueue.poll();
//                } catch (InterruptedException e) {
//                    logger.error("Unexpected: ", e);
//                }
            }
        } else {
            connection = availableQueue.poll();
        }

        addAcquiredConnection(qualifier, connection);
        addPoolConnection(qualifier, connection);
        return connection;
    }

    @Override
    public void release(Connection connection) {
        String qualifier = getQualifierByConnection(connection);

        if (qualifier == null) {
            return;
        }

        removeAcquired(qualifier, connection);
        addAvailableConnection(qualifier, connection);
    }

    private Connection createConnectionByQualifier(String qualifier) throws SQLException {
        if (!dataSources.containsKey(qualifier)) {
            throw new IllegalArgumentException("No database source can be accessed via qualifier \"" + qualifier + "\"");
        }

        DataSource dataSource = dataSources.get(qualifier);
        if (dataSource == null) {
            throw new IllegalArgumentException("No database source can be accessed via qualifier \"" + qualifier + "\"");
        }

        Connection connection = dataSource.getConnection();
        return connection;
    }

    private Connection getConnectionByQualifier(String qualifier) {
        Queue<Connection> connections = available.get(qualifier);
        if (connections == null) {
            return null;
        }

        Connection connection = connections.poll();
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

    private void addPoolConnection(String qualifier, Connection connection) {
        Queue<Connection> poolConnectionsQueue = pool.get(qualifier);
        if (poolConnectionsQueue == null) {
            poolConnectionsQueue = new LinkedBlockingQueue<>();
            pool.put(qualifier, poolConnectionsQueue);
        }

        poolConnectionsQueue.add(connection);
    }

    private void addAvailableConnection(String qualifier, Connection connection) {
        Queue<Connection> availableConnectionsQueue = available.get(qualifier);
        if (availableConnectionsQueue == null) {
            availableConnectionsQueue = new LinkedBlockingQueue<>();
            available.put(qualifier, availableConnectionsQueue);
        }

        availableConnectionsQueue.add(connection);
    }

    private void addAcquiredConnection(String qualifier, Connection connection) {
        Queue<Connection> acquiredConnectionsQueue = acquired.get(qualifier);
        if (acquiredConnectionsQueue == null) {
            acquiredConnectionsQueue = new LinkedBlockingQueue<>();
            acquired.put(qualifier, acquiredConnectionsQueue);
        }

        acquiredConnectionsQueue.add(connection);
    }

    private void removeAvailable(String qualifier, Connection connection) {
        Queue<Connection> availableConnectionQueue = available.get(qualifier);
        availableConnectionQueue.remove(connection);
    }

    private void removeAcquired(String qualifier, Connection connection) {
        Queue<Connection> acquiredConnections = acquired.get(qualifier);
        acquiredConnections.remove(connection);
    }
}
