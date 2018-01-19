package com.tmc.connection.pool.impl;

import com.tmc.connection.pool.def.ConnectionPool;
import com.tmc.exception.SQLConnectionException;
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
import java.util.concurrent.TimeUnit;

public class ConnectionPoolImpl implements ConnectionPool {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final static int DEFAULT_SIZE = 10;

    private final static int CONNECTION_ATTEMPTS = 3;

    private final Map<String, DataSource> dataSources;

    private final Map<String, Queue<Connection>> pool = new ConcurrentSkipListMap<>();

    private final Map<String, Queue<Connection>> available = new ConcurrentSkipListMap<>();

    private final Map<String, Queue<Connection>> acquired = new ConcurrentSkipListMap<>();

    public ConnectionPoolImpl(Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public synchronized Connection acquire(String qualifier) throws SQLConnectionException {
        if (qualifier == null) {
            throw new IllegalArgumentException("Database qualifier cannot be null.");
        }

        Connection connection;

        Queue<Connection> availableQueue = available.get(qualifier);
        if (availableQueue == null) {
            connection = createConnectionByQualifier(qualifier);
            addPoolConnection(qualifier, connection);
        } else if (availableQueue.isEmpty()) {
            Queue<Connection> pollQueue = pool.get(qualifier);
            if (pollQueue.size() <= DEFAULT_SIZE) {
                connection = createConnectionByQualifier(qualifier);
                addPoolConnection(qualifier, connection);
            } else {
                try {
                    while (availableQueue.isEmpty()) {
                        TimeUnit.SECONDS.sleep(1);
                    }
                } catch (InterruptedException e) {
                    logger.error("Unexpected", e);
                }
                connection = availableQueue.poll();
            }
        } else {
            connection = availableQueue.poll();
        }

        try {
            if(connection == null || connection.isClosed()) {
                acquire(qualifier);
            }
        } catch (SQLException e) {
            logger.error("Unexpected: ", e);
        }

        addAcquiredConnection(qualifier, connection);
        return connection;
    }

    @Override
    public void release(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.warn("Connection cannot be released, connection will be closed.", connection);
            destroyConnection(connection);
            return;
        }

        String qualifier = getQualifierByConnection(connection);

        if (qualifier == null) {
            return;
        }

        removeAcquired(qualifier, connection);
        addAvailableConnection(qualifier, connection);
    }

    private void destroyConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error("Unexpected: ", e);
        }

        String qualifier = getQualifierByConnection(connection);
        if (qualifier != null) {
            removeAcquired(qualifier, connection);
            removePool(qualifier, connection);
        }
    }

    private Connection createConnectionByQualifier(String qualifier) throws SQLConnectionException {
        if (!dataSources.containsKey(qualifier)) {
            throw new IllegalArgumentException("No database source can be accessed via qualifier \"" + qualifier + "\"");
        }

        DataSource dataSource = dataSources.get(qualifier);
        if (dataSource == null) {
            throw new IllegalArgumentException("No database source can be accessed via qualifier \"" + qualifier + "\"");
        }

        return createConnectionWithAttempts(dataSource);
    }

    private Connection createConnectionWithAttempts(DataSource dataSource) throws SQLConnectionException {
        Connection connection = null;
        for (int i = 0; i < CONNECTION_ATTEMPTS; i++) {
            try {
                connection = dataSource.getConnection();
                break;
            } catch (SQLException e) {
                logger.warn("Unable to establish connection. Attempt #" + (i + 1));
                if (i == 2) {
                    throw new SQLConnectionException(e);
                }
            }
        }
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

    private void removePool(String qualifier, Connection connection) {
        Queue<Connection> poolConnectionQueue = pool.get(qualifier);
        if (poolConnectionQueue != null) {
            poolConnectionQueue.remove(connection);
        }
    }

    private void removeAcquired(String qualifier, Connection connection) {
        Queue<Connection> acquiredConnections = acquired.get(qualifier);
        if (acquiredConnections != null) {
            acquiredConnections.remove(connection);
        }
    }
}
