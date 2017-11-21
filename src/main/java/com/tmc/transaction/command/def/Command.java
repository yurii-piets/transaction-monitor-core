package com.tmc.transaction.command.def;

import java.sql.SQLException;

/**
 * Basic interface of a Command that could be executed
 */
public interface Command {

    /**
     * Executes the command
     */
    // TODO: 21/11/2017 remove SQLException
    void execute() throws SQLException;
}
