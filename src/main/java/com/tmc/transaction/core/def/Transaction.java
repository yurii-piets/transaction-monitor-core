package com.tmc.transaction.core.def;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;

public interface Transaction extends And {
    And begin(String... qualifiers) throws SQLException;
    And commit();
    And addStatement(String qualifier, String sql) throws SQLException;
    And addStatement(String qualifier, File sql) throws SQLException;
    And addStatement(String qualifier, Path sql) throws SQLException;

    default Transaction and() { return this; }
}
