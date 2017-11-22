package com.tmc.transaction.command.def;

/**
 * Basic interface of a Command that could be reverted
 *
 * @see Command
 */
public interface RevertibleCommand extends Command {

    /**
     * Reverts executed command
     */
    void revert();
}
