package com.tmc.transaction.command.impl;

import com.tmc.exception.SQLQueryException;
import com.tmc.exception.SQLRevertException;
import com.tmc.exception.SQLSavepointCreationException;
import com.tmc.exception.SQLStatementException;
import com.tmc.transaction.command.def.Command;
import com.tmc.transaction.command.def.RevertibleCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * Command that could be executed on a database
 *
 * @see Command
 * @see RevertibleCommand
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DatabaseCommand implements RevertibleCommand {

    /**
     * Connection with the database on which command is executed
     *
     * @see Connection
     */
    private final Connection connection;

    /**
     * Sql query that is executed
     */
    private final String sql;

    /**
     * Savepoint that is created before executing the query
     *
     * @see Savepoint
     */
    private Savepoint savepoint;

    /**
     * Creates statement and execute it on a database that current connection is established on.
     * before execution initialise the Savepoint variable
     *
     * @see Statement
     * @see Savepoint
     */
    @Override
    public void execute() throws SQLQueryException, SQLSavepointCreationException, SQLStatementException {
        initSavepoint();
        Statement statement = createStatement();
        executeStatement(statement);
    }

    /**
     * Reverts execution of a command by reverting to current savepoint
     *
     * @see Savepoint
     */
    @Override
    public void revert() throws SQLRevertException {
        try {
            connection.rollback(savepoint);
        } catch (SQLException e) {
            throw new SQLRevertException(e);
        }
    }

    private void initSavepoint() throws SQLSavepointCreationException {
        try {
            savepoint = connection.setSavepoint();
        } catch (SQLException e) {
            throw new SQLSavepointCreationException(e);
        }
    }

    private Statement createStatement() throws SQLStatementException {
        Statement statement;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new SQLStatementException(e);
        }
        return statement;
    }

    private void executeStatement(Statement statement) throws SQLQueryException {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new SQLQueryException(e, sql);
        }
    }
}
