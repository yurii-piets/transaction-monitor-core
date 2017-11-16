package com.tmc.transaction.core.impl;

import com.tmc.connection.data.DataConnectionManager;
import com.tmc.connection.services.DatabasePropertyService;
import com.tmc.transaction.command.def.Command;
import com.tmc.transaction.command.impl.DatabaseCommand;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.executor.def.CommandsExecutor;
import com.tmc.transaction.executor.impl.DatabaseCommandExecutor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
// TODO: 16/11/2017 add scope mode
public class TransactionImpl implements Transaction {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CommandsExecutor executor = new DatabaseCommandExecutor();

    private final DataConnectionManager dataConnectionManager;

    private final Set<String> activeQualifiers = new HashSet<>();

    private final DatabasePropertyService databasePropertyService;

    @Autowired
    public TransactionImpl(DataConnectionManager dataConnectionManager, DatabasePropertyService databasePropertyService) {
        this.dataConnectionManager = dataConnectionManager;
        this.databasePropertyService = databasePropertyService;
    }

    @Override
    public void begin(String... qualifiers) throws SQLException {
        if (qualifiers == null || qualifiers.length == 0) {
            for (Connection connection : dataConnectionManager.getAllConnections()) {
                turnOffAutoCommit(connection);
                activeQualifiers.addAll(databasePropertyService.getQualifiers());
            }
        } else {
            for (String qualifier : qualifiers) {
                Connection connection = dataConnectionManager.getConnectionByQualifier(qualifier);
                turnOffAutoCommit(connection);
                activeQualifiers.add(qualifier);
            }
        }
    }

    @Override
    public void addStatement(String qualifier, String sql) throws SQLException {
        Connection connection = dataConnectionManager.getConnectionByQualifier(qualifier);
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

    private void commitForAll() {
        for (String qualifier: activeQualifiers){
            Connection connection = dataConnectionManager.getConnectionByQualifier(qualifier);
            try {
                connection.commit();
            } catch (SQLException e) {
                // TODO: 16/11/2017 refactor this
                e.printStackTrace();
            }
        }
    }

    @Override
    public void rollback() {
        executor.revertCommands();
    }
}
