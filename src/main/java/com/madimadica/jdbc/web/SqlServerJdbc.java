package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.BatchInsert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Wrapper for Spring JDBC with SQL Server dialect.
 */
@Component
public class SqlServerJdbc implements JdbcWithExplicitBatchInsertID {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlServerJdbc.class);
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Parameter limit for SQL Server driver
     */
    public static final int MAX_PARAMS_PER_QUERY = 2098;
    /**
     * Insert Limit for SQL Server driver
     */
    public static final int MAX_INSERTS_PER_QUERY = 999;

    /**
     * Construct this as a bean
     * @param jdbc autowired JdbcTemplate instance
     * @param namedJdbc autowired NamedParameterJdbcTemplate instance
     */
    public SqlServerJdbc(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
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

    /**
     * <p>
     *     Wrap an identifier into a standard quoted SQL Server identifier.
     * </p>
     * <p>
     *     For example, <code>foo.bar.baz => [foo].[bar].[baz]</code> or <code>foo.[bar] => [foo].[bar]</code>
     * </p>
     * <p>
     *     Replaces '[' and ']', splits on '.', and joins back together with '[' and ']' around each identifier part.
     * </p>
     *
     * @param identifier String identifier to a properly quoted identifier
     * @return quoted identifier
     */
    @Override
    public String wrapIdentifier(String identifier) {
        identifier = identifier.trim();
        // Replace all '[', ']'
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        int len = identifier.length();
        for (int i = 0; i < len; ++i) {
            char ch = identifier.charAt(i);
            if (ch == '[' || ch == ']') {
                sb.append(identifier, lastStart, i);
                lastStart = i + 1;
            }
        }
        sb.append(identifier, lastStart, len);
        // Split by '.' and join back with escapes
        List<String> parts = InternalUtils.splitChar(sb.toString(), '.');
        StringJoiner fullIdentifier = new StringJoiner(".");
        for (String part : parts) {
            fullIdentifier.add('[' + part + ']');
        }
        return fullIdentifier.toString();
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
