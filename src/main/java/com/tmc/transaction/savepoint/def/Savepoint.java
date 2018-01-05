package com.tmc.transaction.savepoint.def;

import java.sql.SQLException;

public interface Savepoint {

    boolean setSavepoint();

    boolean revert() throws SQLException;
}
