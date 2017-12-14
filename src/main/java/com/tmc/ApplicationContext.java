package com.tmc;

import com.tmc.connection.config.DatabaseConfig;
import com.tmc.connection.services.ConnectionService;
import com.tmc.connection.services.PropertyService;
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

    private final Map<String, DataSource> dataSources = new HashMap<>();

    private ApplicationContext() {
        databaseConfig();
    }

    private DatabaseConfig databaseConfig() {
        if (databaseConfig == null) {
            databaseConfig = new DatabaseConfig(this, propertyService());
        }

        return databaseConfig;
    }

    public ConnectionService connectionService() {
        return new ConnectionService(context);
    }

    public PropertyService propertyService() {
        if (propertyService == null) {
            propertyService = new PropertyService();
        }

        return propertyService;
    }

    public CommandsExecutor commandsExecutor() {
        return new DatabaseCommandExecutor();
    }

    static TransactionService getTransactionService() {
        return new TransactionService(context);
    }

    public DataSource getDataSourceByQualifier(String qualifier) {
        return dataSources.get(qualifier);
    }

    public void addDataSource(String qualifier, DataSource dataSource) {
        dataSources.put(qualifier, dataSource);
    }
}
