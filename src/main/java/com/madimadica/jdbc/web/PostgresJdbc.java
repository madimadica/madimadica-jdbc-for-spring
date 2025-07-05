package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.BatchInsert;
import com.madimadica.jdbc.api.RowInsert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Wrapper for Spring JDBC with Postgres dialect.
 */
@Component
public class PostgresJdbc implements JdbcWithExplicitBatchInsertID, JdbcRowInsertWithExplicitId {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresJdbc.class);
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Parameter limit for Postgres driver
     */
    public static final int MAX_PARAMS_PER_QUERY = 65_535;
    /**
     * Insert Limit for Postgres driver
     */
    public static final int MAX_INSERTS_PER_QUERY = 10_000;

    public PostgresJdbc(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbc;
    }

    @Override
    public NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        return namedJdbc;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public int getMaxParametersPerQuery() {
        return MAX_PARAMS_PER_QUERY;
    }

    @Override
    public int getMaxInsertsPerQuery() {
        return MAX_INSERTS_PER_QUERY;
    }

    @Override
    public <T> List<Number> insertReturningNumbers(BatchInsert<T> batchInsert, String generatedColumn) {
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

        // INSERT INTO [my_table] (a, ..., z) VALUES
        final String sqlPrefix = "INSERT INTO " +
                wrapIdentifier(batchInsert.tableName()) +
                sjColumnNames +
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
                "Batch inserting {} rows ({} {}): {}{} RETURNING {}",
                rows.size(),
                batches.size(),
                batches.size() == 1 ? "batch" : "batches",
                sqlPrefix,
                valuesTemplate,
                generatedColumn
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
            // INSERT INTO my_table (cols...) VALUES (...) RETURNING generated_column
            currentBatchSql.append(" RETURNING ").append(generatedColumn);
            List<Number> batchIds = getJdbcTemplate().queryForList(
                    currentBatchSql.toString(),
                    Number.class,
                    params
            );
            generatedValues.addAll(batchIds);
        }

        return generatedValues;
    }

    @Override
    public String wrapIdentifier(String identifier) {
        return identifier;
    }

    @Override
    public Number insertReturningNumber(RowInsert rowInsert, String column) {
        String sql = InternalUtils.generateInsertSql(this, rowInsert);
        sql += " RETURNING " + column;
        getLogger().debug(sql);
        return getJdbcTemplate().queryForObject(sql, Number.class, rowInsert.escapedValues().values().toArray());
    }

    @Override
    public <T> T insertReturning(RowInsert rowInsert, RowMapper<T> rowMapper) {
        String sql = InternalUtils.generateInsertSql(this, rowInsert);
        sql += " RETURNING *";
        return this.queryOne(sql, rowMapper, rowInsert.escapedValues().values().toArray()).orElse(null);
    }
}
