package com.tmc.transaction.command.def;

/**
 * Basic interface of a Command that could be executed
 */
public interface Command {

    /**
     * Executes the command
     */
    void execute() throws Exception;
}
