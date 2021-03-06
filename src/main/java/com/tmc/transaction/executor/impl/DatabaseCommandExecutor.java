package com.tmc.transaction.executor.impl;

import com.tmc.transaction.command.def.Command;
import com.tmc.transaction.command.def.RevertibleCommand;
import com.tmc.transaction.executor.def.CommandsExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class DatabaseCommandExecutor implements CommandsExecutor {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Queue of command that will be executed on executeCommands method call
     */
    private final Queue<Command> commands = new LinkedList<>();

    /**
     * Stack of commands that was executed with success
     */
    private final Deque<Command> applied = new LinkedList<>();

    @Override
    public boolean addCommand(Command command) {
        return commands.add(command);
    }

    @Override
    public boolean removeCommand(Command command) {
        return commands.remove(command);
    }

    @Override
    public void clearCommands() {
        commands.clear();
    }

    @Override
    public void executeCommands() throws Exception {
        while (!commands.isEmpty()) {
            Command command = commands.poll();
            command.execute();
            applied.addFirst(command);
        }
    }

    @Override
    public void revertCommands() {
        while (!applied.isEmpty()) {
            Command command = applied.pollFirst();
            if (command instanceof RevertibleCommand) {
                RevertibleCommand revertibleCommand = (RevertibleCommand) command;
                try {
                    //revertibleCommand.revert();
                } catch (Exception e) {
                    logger.error("Unexpected: ", e);
                }
            }
        }
    }
}
