package com.tmc.transaction.core.def;

import java.sql.SQLException;

public interface Transaction {
    void begin(String... qualifiers) throws SQLException;
    void commit();
    void addStatement(String qualifier, String sql) throws SQLException;
    void rollback();
}
