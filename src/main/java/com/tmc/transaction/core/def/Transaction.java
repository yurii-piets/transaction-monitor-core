package com.tmc.transaction.core.def;

import java.sql.SQLException;

public interface Transaction extends And {
    And begin(String... qualifiers) throws SQLException;
    And commit();
    And addStatement(String qualifier, String sql) throws SQLException;
    And rollback();

    default Transaction and() { return this;}
}
