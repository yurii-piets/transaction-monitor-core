package com.tmc.transaction.executor.def;

import com.tmc.transaction.command.def.Command;

import java.sql.SQLException;

public interface CommandsExecutor {

    boolean addCommand(Command command);
    boolean removeCommand(Command command);
    void clearCommands();

    void executeCommands() throws SQLException;
    void revertCommands();
}
