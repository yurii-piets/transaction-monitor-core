package com.tmc.transaction.core.def;

import com.tmc.exception.SQLConnectionException;

import java.io.File;
import java.nio.file.Path;

/**
 * Class for performing transaction on multiple databases
 * implements Two-Phase Commit Mechanism
 */
public interface Transaction extends And {

    /**
     * Begins a new transaction
     *
     * @param qualifiers transaction is open on a databases specified by the qualifiers
     */
    And begin(String... qualifiers);

    /**
     * Add query that will be executed in current transaction
     *
     * @param qualifier of database on which current query will be executed
     * @param query query that will be executed
     */
    And addStatement(String qualifier, String query) throws SQLConnectionException;

    /**
     * Add queries that will be executed in current transaction
     *
     * @param qualifier of database on which current queries will be executed
     * @param file instance of file that contains sql queries
     */
    And addStatement(String qualifier, File file);

    /**
     * Add queries that will be executed in current transaction
     *
     * @param qualifier of database on which current queries will be executed
     * @param path to a file that contains sql queries
     *
     * @see Path
     */
    And addStatement(String qualifier, Path path);

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
