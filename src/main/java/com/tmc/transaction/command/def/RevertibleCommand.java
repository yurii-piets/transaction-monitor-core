package com.tmc.transaction.command.def;

import java.sql.SQLException;

/**
 * Basic interface of a Command that could be reverted
 *
 * @see Command
 */
public interface RevertibleCommand extends Command {

    /**
     * Reverts executed command
     */
    // TODO: 21/11/2017 remove SQLException
    void revert() throws SQLException;
}
