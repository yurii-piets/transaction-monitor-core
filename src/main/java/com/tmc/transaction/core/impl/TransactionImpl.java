package com.tmc.transaction.core.impl;

import com.tmc.connection.services.ConnectionService;
import com.tmc.connection.services.PropertyService;
import com.tmc.transaction.command.def.Command;
import com.tmc.transaction.command.impl.DatabaseCommand;
import com.tmc.transaction.core.def.And;
import com.tmc.transaction.core.def.Transaction;
import com.tmc.transaction.executor.def.CommandsExecutor;
import com.tmc.transaction.executor.impl.DatabaseCommandExecutor;
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
public class TransactionImpl implements Transaction {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConnectionService connectionService;

    private final PropertyService propertyService;

    private final CommandsExecutor executor = new DatabaseCommandExecutor();

    private final Set<String> activeQualifiers = new HashSet<>();

    @Autowired
    public TransactionImpl(ConnectionService connectionService,
                           PropertyService propertyService) {
        this.connectionService = connectionService;
        this.propertyService = propertyService;
    }

    @Override
    public And begin(String... qualifiers) throws SQLException {
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
        return this;
    }

    @Override
    public And addStatement(String qualifier, String query) throws SQLException {
        Connection connection = connectionService.getConnectionByQualifier(qualifier);

        String filteredQuery = filterQuery(query);

        Command command = new DatabaseCommand(connection, filteredQuery);
        executor.addCommand(command);

        return this;
    }

    @Override
    public And addStatement(String qualifier, File file) throws SQLException {
        try {
            String query = Files.readAllLines(file.toPath())
                    .stream()
                    .collect(Collectors.joining());
            addStatement(qualifier, query);
        } catch (IOException e) {
            logger.error("Unexpected: ", e);
        }

        return this;
    }

    @Override
    public And addStatement(String qualifier, Path path) throws SQLException {
        try {
            String query = Files.readAllLines(path)
                    .stream()
                    .collect(Collectors.joining());

            addStatement(qualifier, query);
        } catch (IOException e) {
            logger.error("Unexpected: ", e);
        }

        return this;
    }

    @Override
    public And commit() {
        try {
            executor.executeCommands();
            commitForAll();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            executor.revertCommands();
            logger.info("Applied revert on databases.");
        }

        return this;
    }

    @PreDestroy
    public void finalize() {
        for (String qualifier: propertyService.getQualifiers()){
            try {
                Connection connection = connectionService.getConnectionByQualifier(qualifier);
                connection.close();
            } catch (SQLException e) {
                logger.error("Unexpected error while closing connection", e);
            }
        }
        connectionService.clearCache();
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

    private void turnOffAutoCommit(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
    }

    private String filterQuery(String query) {
        String filteredQuery = query
                .replace("begin;", "")
                .replace("commit;", "");
        return filteredQuery;
    }
}
