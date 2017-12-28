package com.tmc;

import com.tmc.connection.config.DatabaseConfig;
import com.tmc.connection.pool.def.ConnectionPool;
import com.tmc.connection.pool.impl.ConnectionPoolImpl;
import com.tmc.connection.services.ConnectionService;
import com.tmc.connection.services.PropertyService;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.core.impl.TransactionImpl;
import com.tmc.transaction.executor.def.CommandsExecutor;
import com.tmc.transaction.executor.impl.DatabaseCommandExecutor;
import com.tmc.transaction.service.TransactionService;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public final class ApplicationContext {

    private static final ApplicationContext context = new ApplicationContext();

    private DatabaseConfig databaseConfig;

    private PropertyService propertyService;

    private ConnectionPool connectionPool;

    private Map<String, DataSource> dataSources;

    private ApplicationContext() {
        databaseConfig();
    }

    private DatabaseConfig databaseConfig() {
        if (databaseConfig == null) {
            databaseConfig = new DatabaseConfig(dataSources(), propertyService());
        }

        return databaseConfig;
    }

    private PropertyService propertyService() {
        if (propertyService == null) {
            propertyService = new PropertyService();
        }

        return propertyService;
    }

    private ConnectionPool connectionPool() {
        if (connectionPool == null) {
            connectionPool = new ConnectionPoolImpl(dataSources());
        }

        return connectionPool;
    }

    private ConnectionService connectionService() {
        return new ConnectionService(connectionPool());
    }

    private CommandsExecutor commandsExecutor() {
        return new DatabaseCommandExecutor();
    }

    private Map<String, DataSource> dataSources() {
        if (dataSources == null) {
            dataSources = new HashMap<>();
        }

        return dataSources;
    }

    static TransactionService transactionService() {
        return new TransactionService(context);
    }

    public Transaction transaction() {
        return new TransactionImpl(connectionService(), commandsExecutor());
    }
}
