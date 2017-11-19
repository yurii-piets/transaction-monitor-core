package com.tmc.transaction.command.impl;

import com.tmc.transaction.command.def.RevertibleCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DatabaseCommand implements RevertibleCommand {

    private final Connection connection;

    private final String sql;

    private Savepoint savepoint;

    @Override
    public void execute() throws SQLException {
        savepoint = connection.setSavepoint();

        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    @Override
    public void revert() throws SQLException {
        connection.releaseSavepoint(savepoint);
    }
}
