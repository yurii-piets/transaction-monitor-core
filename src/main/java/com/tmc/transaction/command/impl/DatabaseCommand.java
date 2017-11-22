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
    public void execute() {
        try {
            savepoint = connection.setSavepoint();
        } catch (SQLException e) {
            throw new SQLSavepointCreationException(e);
        }

        Statement statement;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new SQLStatementException(e);
        }

        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new SQLQueryException(e);
        }
    }

    /**
     * Reverts execution of a command by releasing current savepoint
     *
     * @see Savepoint
     */
    @Override
    public void revert() {
        try {
            connection.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            throw new SQLRevertException(e);
        }
    }
}
