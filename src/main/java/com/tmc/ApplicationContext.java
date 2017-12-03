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

    private ConnectionService connectionService;

    private PropertyService propertyService;

    private CommandsExecutor commandsExecutor;

    private Map<String, DataSource> dataSources = new HashMap<>();

    private ApplicationContext(){
        databaseConfig();
    }

    DatabaseConfig databaseConfig() {
        if (databaseConfig == null) {
            databaseConfig = new DatabaseConfig(this, propertyService());
        }

        return databaseConfig;
    }

    public ConnectionService connectionService() {
        if (connectionService == null) {
            connectionService = new ConnectionService(context);
        }

        return connectionService;
    }

    public PropertyService propertyService() {
        if (propertyService == null) {
            propertyService = new PropertyService();
        }

        return propertyService;
    }

    public CommandsExecutor commandsExecutor() {
        if (commandsExecutor == null) {
            return new DatabaseCommandExecutor();
        }

        return commandsExecutor;
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
