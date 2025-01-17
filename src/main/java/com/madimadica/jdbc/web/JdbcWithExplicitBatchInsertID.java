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
     * @param tableName Name of the table to update
     * @param <T> type of rows to map
     * @param rows list of rows to map to inserts
     * @return fluent API builder to finish defining the insert.
     */
    default <T> BatchInsertBuilderSteps.RequiredValue<T, BatchInsertBuilderSteps.AdditionalValuesWithExplicitID<T>> batchInsertInto(String tableName, List<T> rows) {
        getLogger().debug("Using [batchInsertInto] (Explicit IDs) API");
        return new BatchInsertBuilderWithExplicitId<>(this, tableName, rows);
    }

    /**
     * Get the maximum number of parameter bindings allowed per query by the database driver
     * @return the limit for the database driver
     */
    int getMaxParametersPerQuery();

    /**
     * Get the maximum number of inserts allowed per query by the database driver
     * @return the limit for the database driver
     */
    int getMaxInsertsPerQuery();

    /**
     * Execute a batch insert operation and return a list of generated keys.
     * @param batchInsert batch insert configuration parameter
     * @param generatedColumn the name of the column to output generated values from
     * @return list of generated keys per row insert.
     * @param <T> type of rows to map to inserts
     */
    default <T> List<Number> insertReturningNumbers(BatchInsert<T> batchInsert, String generatedColumn) {
        final List<T> rows = batchInsert.rows();
        if (rows.isEmpty()) {
            getLogger().debug("No rows in batch insert");
            return List.of();
        }
        StringJoiner sjColumnNames = new StringJoiner(", ", " (", ") ");
        for (String col : batchInsert.escapedMappings().keySet()) {
            sjColumnNames.add(col);
        }
        for (String col : batchInsert.escapedConstants().keySet()) {
            sjColumnNames.add(col);
        }
        for (String col : batchInsert.unescapedConstants().keySet()) {
            sjColumnNames.add(col);
        }

        // INSERT INTO [my_table] (a, ..., z) OUTPUT INSERTED.[my_id] VALUES
        final String sqlPrefix = "INSERT INTO " +
                wrapIdentifier(batchInsert.tableName()) +
                sjColumnNames +
                "OUTPUT INSERTED." +
                wrapIdentifier(generatedColumn) +
                " VALUES ";

        final var rowMappers = batchInsert.escapedMappings().values();
        final var escapedConstants = batchInsert.escapedConstants().values();
        final int paramsPerRow = rowMappers.size() + escapedConstants.size();

        StringJoiner valuesTemplateJoiner = new StringJoiner(",", "(", ")");
        for (int i = 0; i < paramsPerRow; ++i) {
            valuesTemplateJoiner.add("?");
        }
        for (Object constant : batchInsert.unescapedConstants().values()) {
            valuesTemplateJoiner.add(String.valueOf(constant));
        }
        // "(?,?,?,GETDATE())"
        final String valuesTemplate = valuesTemplateJoiner.toString();
        // ",(?,?,?,GETDATE())"
        final String valuesTemplateWithComma = "," + valuesTemplate;


        int batchSize = rows.size();
        if (paramsPerRow * batchSize > getMaxParametersPerQuery()) {
            batchSize = getMaxParametersPerQuery() / paramsPerRow;
        }
        if (batchSize > getMaxInsertsPerQuery()) {
            batchSize = getMaxInsertsPerQuery();
        }

        final List<List<T>> batches = InternalUtils.partitionBySize(rows, batchSize);
        final List<Number> generatedValues = new ArrayList<>(rows.size());


        getLogger().debug(
                "Batch inserting {} rows ({} {}): {}{}",
                rows.size(),
                batches.size(),
                batches.size() == 1 ? "batch" : "batches",
                sqlPrefix,
                valuesTemplate
        );

        for (List<T> batch : batches) {
            final int rowCount = batch.size();
            final int parameterCount = rowCount * paramsPerRow;
            final Object[] params = new Object[parameterCount];
            int paramIndex = 0;
            for (int i = 0; i < rowCount; i++) {
                T row = batch.get(i);
                for (var mapper : rowMappers) {
                    params[paramIndex++] = mapper.apply(row);
                }
                for (var constant : escapedConstants) {
                    params[paramIndex++] = constant;
                }
            }
            if (parameterCount != paramIndex) {
                throw new AssertionError("Expected %d parameters, only added %d".formatted(parameterCount, paramIndex));
            }
            StringBuilder currentBatchSql = new StringBuilder(sqlPrefix);
            currentBatchSql.append(valuesTemplate);
            if (rowCount > 1) {
                currentBatchSql.repeat(valuesTemplateWithComma, rowCount - 1);
            }
            List<Number> batchIds = getJdbcTemplate().queryForList(
                    currentBatchSql.toString(),
                    Number.class,
                    params
            );
            generatedValues.addAll(batchIds);
        }

        return generatedValues;
    }
}
