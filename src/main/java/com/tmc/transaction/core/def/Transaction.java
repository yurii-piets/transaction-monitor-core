package com.tmc.transaction.core.def;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Class for performing transaction on multiple databases
 * implements Two-Phase Commit Mechanism
 */
public interface Transaction extends And {

    /**
     * Begins a new transaction
     *
     * @param qualifiers transaction is open on a databases specified by the qualifiers
     * @throws SQLException if transaction could not be began on one of the databases
     */
    And begin(String... qualifiers) throws SQLException;

    /**
     * Add query that will be executed in current transaction
     *
     * @param qualifier of database on which current query will be executed
     * @param query query that will be executed
     * @throws SQLException query cannot be executed
     */
    And addStatement(String qualifier, String query) throws SQLException;

    /**
     * Add queries that will be executed in current transaction
     *
     * @param qualifier of database on which current queries will be executed
     * @param file instance of file that contains sql queries
     * @throws SQLException if one of the queries cannot be executed
     */
    And addStatement(String qualifier, File file) throws SQLException;

    /**
     * Add queries that will be executed in current transaction
     *
     * @param qualifier of database on which current queries will be executed
     * @param path to a file that contains sql queries
     * @throws SQLException if one of the queries cannot be executed
     *
     * @see Path
     */
    And addStatement(String qualifier, Path path) throws SQLException;

    /**
     * Commits queries to all databases
     */
    And commit();

    /**
     * makes possible to call methods of this class in sequence
     * @return this object
     */
    default Transaction and() { return this; }
}
