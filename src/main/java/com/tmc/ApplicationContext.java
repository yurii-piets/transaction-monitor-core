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

/**
 * Context of application,
 * is created on TransactionService.boot() first method call
 */
final class ApplicationContext {

    private static ApplicationContext context;

    private static TransactionService transactionService;

    private DatabaseConfig databaseConfig;

    private PropertyService propertyService;

    private ConnectionPool connectionPool;

    private Map<String, DataSource> dataSources;

    private ApplicationContext() {
        config();
    }

    /**
     * Configs all classes that are needed to be configured on application startup
     */
    private void config() {
        databaseConfig();
    }

    /**
     * Initializes database config class
     */
    private void databaseConfig() {
        if (databaseConfig == null) {
            databaseConfig = new DatabaseConfig(dataSources(), propertyService());
        }
    }

    /**
     * @return instance of PropertyService, initializes propertyService in case it it was not
     * @see PropertyService
     */
    private PropertyService propertyService() {
        if (propertyService == null) {
            propertyService = new PropertyService();
        }

        return propertyService;
    }

    /**
     * @return instance of ConnectionPool, initializes propertyService in case it it was not@return
     * @see ConnectionPool
     */
    private ConnectionPool connectionPool() {
        if (connectionPool == null) {
            connectionPool = new ConnectionPoolImpl(dataSources());
        }

        return connectionPool;
    }

    /**
     * @return instance of ConnectionService, initializes in case it it was not
     * @see ConnectionService
     */
    private ConnectionService connectionService() {
        return new ConnectionService(connectionPool());
    }

    /**
     * @return instance of CommandsExecutor, initializes in case it it was not
     * @see CommandsExecutor
     */
    private CommandsExecutor commandsExecutor() {
        return new DatabaseCommandExecutor();
    }

    /**
     * @return initialized data sources, initializes in case they were not
     * @see DataSource
     */
    private Map<String, DataSource> dataSources() {
        if (dataSources == null) {
            dataSources = new HashMap<>();
        }

        return dataSources;
    }

    /**
     * @return ApplicationContext, initializes whole application context in case it it was not
     */
    private static ApplicationContext context() {
        if (context == null) {
            context = new ApplicationContext();
        }

        return context;
    }

    /**
     * @return instance of TransactionService, initializes in case it it was not
     * @see TransactionService
     */
    static TransactionService transactionService() {
        if (transactionService == null) {
            transactionService = new TransactionService(context());
        }

        return transactionService;
    }

    /**
     * @return new instance of transaction for each method call
     * @see Transaction
     */
    public Transaction transaction() {
        return new TransactionImpl(connectionService(), commandsExecutor());
    }
}
