package com.tmc.transaction.command.impl;

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
     * @throws SQLException if sql query could not be executed
     */
    @Override
    public void execute() throws SQLException {
        savepoint = connection.setSavepoint();

        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    /**
     * Reverts execution of a command by releasing current savepoint
     *
     * @see Savepoint
     * @throws SQLException in most cases if savepoint could not be released is current Transaction
     */
    @Override
    public void revert() throws SQLException {
        connection.releaseSavepoint(savepoint);
    }
}
