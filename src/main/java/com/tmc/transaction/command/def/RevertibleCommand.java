package com.tmc.transaction.command.def;

import java.sql.SQLException;

public interface RevertibleCommand extends Command {
    void revert() throws SQLException;
}
