package com.tmc.transaction.core.impl;

import com.tmc.connection.services.ConnectionService;
import com.tmc.connection.services.PropertyService;
import com.tmc.exception.SQLAutoCommitException;
import com.tmc.exception.SQLConnectionException;
import com.tmc.transaction.command.def.Command;
import com.tmc.transaction.command.impl.DatabaseCommand;
import com.tmc.transaction.core.def.And;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.executor.def.CommandsExecutor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class TransactionImpl implements Transaction {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConnectionService connectionService;

    private final PropertyService propertyService;

    private final CommandsExecutor executor;

    /**
     * Set  qualifiers od databases on which current transaction is performed
     */
    private final Set<String> activeQualifiers = new HashSet<>();

    @Autowired
    public TransactionImpl(ConnectionService connectionService,
                           PropertyService propertyService,
                           CommandsExecutor executor) {
        this.connectionService = connectionService;
        this.propertyService = propertyService;
        this.executor = executor;
    }

    @Override
    public And begin(String... qualifiers) {
        try {

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
        } catch (SQLAutoCommitException | SQLConnectionException e) {
            logger.error("Unexpected: ", e);
        }
        return this;
    }

    @Override
    public And addStatement(String qualifier, String query) throws SQLConnectionException {
        Connection connection = connectionService.getConnectionByQualifier(qualifier);

        String filteredQuery = filterQuery(query);

        Command command = new DatabaseCommand(connection, filteredQuery);
        executor.addCommand(command);

        return this;
    }

    @Override
    public And addStatement(String qualifier, File file) {
        try {
            String query = Files.readAllLines(file.toPath())
                    .stream()
                    .collect(Collectors.joining());
            addStatement(qualifier, query);
        } catch (IOException | SQLConnectionException e) {
            logger.error("Unexpected: ", e);
        }

        return this;
    }

    @Override
    public And addStatement(String qualifier, Path path) {
        try {
            String query = Files.readAllLines(path)
                    .stream()
                    .collect(Collectors.joining());

            addStatement(qualifier, query);
        } catch (IOException | SQLConnectionException e) {
            logger.error("Unexpected: ", e);
        }

        return this;
    }

    @Override
    public And commit() {
        try {
            executor.executeCommands();
        } catch (Exception e) {
            logger.error("Unexpected: ", e);
        }
        commitForAll();

        return this;
    }

    /**
     * Method that should be called on Bean/object destruction
     * close opened connection in current transaction
     */
    @PreDestroy
    public void finalize() {
        for (String qualifier : propertyService.getQualifiers()) {
            try {
                Connection connection = connectionService.getConnectionByQualifier(qualifier);
                connection.close();
            } catch (SQLException e) {
                logger.error("Unexpected error while closing connection", e);
            } catch (SQLConnectionException e) {
                logger.error("Unexpected: ", e);
            }
        }
        connectionService.clearCache();
    }

    /**
     * Performs commit on all databases that were specified by qualifier in current transaction
     */
    private void commitForAll() {
        for (String qualifier : activeQualifiers) {
            try {
                Connection connection = connectionService.getConnectionByQualifier(qualifier);
                connection.commit();
            } catch (SQLException | SQLConnectionException e) {
                logger.error("Unexpected: ", e);
            }
        }
    }

    /**
     * Turns down auto commit options for connection
     *
     * @param connection for which auto-commit will be turned down
     */
    private void turnOffAutoCommit(Connection connection) throws SQLAutoCommitException {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new SQLAutoCommitException(e);
        }
    }

    /**
     * Removes string "begin;" and "commit;" from query
     *
     * @param query that is filtered
     * @return filtered query
     */
    private String filterQuery(String query) {
        String filteredQuery = query
                .replace("begin;", "")
                .replace("commit;", "");
        return filteredQuery;
    }
}
