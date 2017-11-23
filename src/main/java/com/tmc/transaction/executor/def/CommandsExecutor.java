package com.tmc.transaction.executor.def;

import com.tmc.transaction.command.def.Command;

public interface CommandsExecutor {

    /**
     * Adds new command to the collection that will be executed
     *
     * @param command that is added to the collection
     * @return true is command was added to the collection, false if was not
     * @see Command
     */
    boolean addCommand(Command command);

    /**
     * Removes command from the collection of the command that will be executed
     *
     * @param command that should be removed
     * @return true is the command was removed from the queue
     * @see Command
     */
    boolean removeCommand(Command command);

    /**
     * Clear all collection of the commands
     */
    void clearCommands();

    /**
     * Executes one-by-one command from the collection of the commands
     */
    void executeCommands() throws Exception;

    /**
     * Revert one-by-one command from the collection of the executed commands
     */
    void revertCommands();
}
