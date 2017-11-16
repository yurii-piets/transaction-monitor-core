package com.tmc.transaction.command.def;

import java.sql.SQLException;

public interface Command {
    void execute() throws SQLException;
    void revert() throws SQLException;
}
