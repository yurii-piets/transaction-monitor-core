package com.tmc.transaction.core.impl;

import com.tmc.connection.services.ConnectionService;
import com.tmc.connection.services.PropertyService;
import com.tmc.transaction.command.def.Command;
import com.tmc.transaction.command.impl.DatabaseCommand;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.executor.def.CommandsExecutor;
import com.tmc.transaction.executor.impl.DatabaseCommandExecutor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransactionImpl implements Transaction {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CommandsExecutor executor = new DatabaseCommandExecutor();

    private final ConnectionService connectionService;

    private final Set<String> activeQualifiers = new HashSet<>();

    private final PropertyService propertyService;

    @Autowired
    public TransactionImpl(ConnectionService connectionService,
                           PropertyService propertyService) {
        this.connectionService = connectionService;
        this.propertyService = propertyService;
    }

    @Override
    public void begin(String... qualifiers) throws SQLException {
        if (qualifiers == null || qualifiers.length == 0) {
            for (Connection connection : connectionService.getAllConnections()) {
                turnOffAutoCommit(connection);
                activeQualifiers.addAll(propertyService.getQualifiers());
            }
        } else {
            for (String qualifier : qualifiers) {
                Connection connection = connectionService.getConnectionByQualifier(qualifier);
                turnOffAutoCommit(connection);
                activeQualifiers.add(qualifier);
            }
        }
    }

    @Override
    public void addStatement(String qualifier, String sql) throws SQLException {
        Connection connection = connectionService.getConnectionByQualifier(qualifier);
        Command command = new DatabaseCommand(connection, sql);
        executor.addCommand(command);
    }

    @Override
    public void commit() {
        try {
            executor.executeCommands();
            commitForAll();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            executor.revertCommands();
            logger.info("Applied revert on databases.");
        }
    }

    private void turnOffAutoCommit(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
    }

    private void commitForAll() throws SQLException {
        for (String qualifier : activeQualifiers) {
            Connection connection = connectionService.getConnectionByQualifier(qualifier);
            try {
                connection.commit();
            } catch (SQLException e) {
                logger.error("Unexpected error while performing commit to database.", e);
            }
        }
    }

    @Override
    public void rollback() {
        executor.revertCommands();
    }
}
