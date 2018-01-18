package com.tmc.transaction.core.impl;

import com.tmc.connection.services.ConnectionService;
import com.tmc.exception.SQLAutoCommitException;
import com.tmc.exception.SQLConnectionException;
import com.tmc.transaction.command.impl.DatabaseCommand;
import com.tmc.transaction.core.def.And;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.executor.def.CommandsExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TransactionImpl implements Transaction {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConnectionService connectionService;

    private final CommandsExecutor executor;

    /**
     * Set  qualifiers od databases on which current transaction is performed
     */
    private final Set<String> activeQualifiers = new HashSet<>();

    public TransactionImpl(ConnectionService connectionService,
                           CommandsExecutor executor) {
        this.connectionService = connectionService;
        this.executor = executor;
    }

    @Override
    public And begin(String... qualifiers) {
        if (qualifiers == null || qualifiers.length == 0) {
            throw new IllegalArgumentException("Qualifiers of databases on which transaction will be performed should be listed.");
        }

        try {
            for (String qualifier : qualifiers) {
                Connection connection = connectionService.getConnectionByQualifier(qualifier);
                turnOffAutoCommit(connection);
                activeQualifiers.add(qualifier);
            }
        } catch (SQLAutoCommitException | SQLConnectionException e) {
            logger.error("Unexpected: ", e);
        }
        return this;
    }

    @Override
    public And addStatement(String qualifier, String query) {
        try {
            Connection connection = connectionService.getConnectionByQualifier(qualifier);

            String filteredQuery = filterQuery(query);

            Arrays.stream(filteredQuery.split(";"))
                    .filter(Objects::nonNull)
                    .filter(statement -> !statement.isEmpty())
                    .filter(statement -> !statement.matches("^\n*$"))
                    .map(statement -> statement + ";")
                    .map(statement -> new DatabaseCommand(connection, statement))
                    .forEach(executor::addCommand);

//            Command command = new DatabaseCommand(connection, filteredQuery);
//            executor.addCommand(command);
        } catch (SQLConnectionException e) {
            logger.error("Unexpected: ", e);
        }

        return this;
    }

    @Override
    public And addStatement(String qualifier, Path path) throws IOException {
        String query = Files.readAllLines(path)
                .stream()
                .collect(Collectors.joining("\n"));

        addStatement(qualifier, query);
        return this;
    }

    @Override
    public And addStatement(String qualifier, File file) throws IOException {
        addStatement(qualifier, file.toPath());
        return this;
    }

    @Override
    public void commit() {
        try {
            logger.info("Performing transaction.");
            executor.executeCommands();
            commitForAll();
            logger.info("Transaction successfully finished.");
        } catch (Exception e) {
            logger.error("Unexpected: " + e.getLocalizedMessage(), e.getCause());
            executor.revertCommands();
            executor.clearCommands();
            logger.error("Applied revert on database.");
        }

        finishTransaction();
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
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
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
        String FORBIDDEN_STATEMENTS_REGEX = "(?i)\\b" + new ArrayList<String>() {{
            add("begin;");
            add("begin transaction;");
            add("start transaction;");
            add("commit;");
            add("commit transaction;");
            add("rollback;");
            add("rollback transaction;");
            add("end;");
        }}.stream()
                .map(s -> "(" + s + ")")
                .collect(Collectors.joining("|"));

        return query.replaceAll(FORBIDDEN_STATEMENTS_REGEX, "");
    }

    /**
     * Method that is called when transaction is committed
     * close opened connection in current transaction
     */
    private void finishTransaction() {
        connectionService.releaseConnections();
    }
}
