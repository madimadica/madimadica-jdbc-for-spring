package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.BatchInsert;
import com.madimadica.jdbc.api.BatchInsertBuilderWithImplicitId;
import com.madimadica.jdbc.api.BatchInsertBuilderSteps;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Defines a JDBC source that can implicitly handle generated ID outputs for batch inserts.
 */
public interface JdbcWithImplicitBatchInsertID extends MadimadicaJdbc {
    /**
     * Begin a sequence of fluent API operations to define a batch of
     * row INSERT INTO queries. Terminated (and executed) by a call to insert,
     * with or without a returning generated values.
     * @param tableName Name of the table to update
     * @param <T> type of rows to map
     * @param rows list of rows to map to inserts
     * @return fluent API builder to finish defining the insert.
     */
    default <T> BatchInsertBuilderSteps.RequiredValue<T, BatchInsertBuilderSteps.AdditionalValuesWithImplicitID<T>> batchInsertInto(String tableName, List<T> rows) {
        getLogger().trace("Using [batchInsertInto] (Implicit IDs) API");
        return new BatchInsertBuilderWithImplicitId<>(this, tableName, rows);
    }

    /**
     * Execute a batch insert operation and return an array of generated keys.
     * @param batchInsert batch insert configuration parameter
     * @return list of generated keys per row insert.
     * @param <T> type of rows to map to inserts
     */
    default <T> List<Number> insertReturningNumbers(BatchInsert<T> batchInsert) {
        if (batchInsert.rows().isEmpty()) {
            getLogger().debug("No rows in batch insert");
            return List.of();
        }
        List<T> rows = batchInsert.rows();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Collection<Function<? super T, Object>> mappings = batchInsert.escapedMappings().values();
        Collection<Object> escapedConstants = batchInsert.escapedConstants().values();

        String sql = InternalUtils.generateInsertSql(this, batchInsert);
        getLogger().debug("Batch inserting {} rows: {}", rows.size(), sql);

        getJdbcTemplate().batchUpdate(
                connection -> connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        T row = rows.get(i);
                        int paramIndex = 1;
                        for (var fn : mappings) {
                            ps.setObject(paramIndex++, fn.apply(row));
                        }
                        for (var constant : escapedConstants) {
                            ps.setObject(paramIndex++, constant);
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return rows.size();
                    }
                },
                keyHolder
        );

        List<Number> generatedKeys = new ArrayList<>();
        for (var rowKeys : keyHolder.getKeyList()) {
            Object firstKey = new ArrayList<>(rowKeys.values()).getFirst();
            if (firstKey instanceof Number number) {
                generatedKeys.add(number);
            } else {
                throw new DataRetrievalFailureException("The generated key type is not an instanceof Number, found " + firstKey.getClass().getName());
            }
        }
        return generatedKeys;
    }
}
