package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.BatchInsert;
import com.madimadica.jdbc.api.BatchInsertBuilderSteps;
import com.madimadica.jdbc.api.BatchInsertBuilderWithExplicitId;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Defines a JDBC source that requires explicit generated ID outputs for batch inserts.
 */
public interface JdbcWithExplicitBatchInsertID extends MadimadicaJdbc {

    /**
     * Begin a sequence of fluent API operations to define a batch of
     * row INSERT INTO queries. Terminated (and executed) by a call to insert,
     * with or without a returning generated values.
     *
     * @param tableName Name of the table to update
     * @param <T>       type of rows to map
     * @param rows      list of rows to map to inserts
     * @return fluent API builder to finish defining the insert.
     */
    default <T> BatchInsertBuilderSteps.RequiredValue<T, BatchInsertBuilderSteps.AdditionalValuesWithExplicitID<T>> batchInsertInto(String tableName, List<T> rows) {
        getLogger().trace("Using [batchInsertInto] (Explicit IDs) API");
        return new BatchInsertBuilderWithExplicitId<>(this, tableName, rows);
    }

    /**
     * Get the maximum number of parameter bindings allowed per query by the database driver
     *
     * @return the limit for the database driver
     */
    int getMaxParametersPerQuery();

    /**
     * Get the maximum number of inserts allowed per query by the database driver
     *
     * @return the limit for the database driver
     */
    int getMaxInsertsPerQuery();

    /**
     * Execute a batch insert operation and return a list of generated keys.
     *
     * @param batchInsert     batch insert configuration parameter
     * @param generatedColumn the name of the column to output generated values from
     * @param <T>             type of rows to map to inserts
     * @return list of generated keys per row insert.
     */
    <T> List<Number> insertReturningNumbers(BatchInsert<T> batchInsert, String generatedColumn);
}
