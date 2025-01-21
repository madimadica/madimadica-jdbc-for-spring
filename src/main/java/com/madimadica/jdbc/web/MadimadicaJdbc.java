package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.*;
import org.slf4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;

/**
 * API Specification for a Jdbc wrapper class.
 */
public interface MadimadicaJdbc {

    /**
     * Get the underlying JdbcTemplate instance
     * @return {@link JdbcTemplate} bean
     */
    JdbcTemplate getJdbcTemplate();

    /**
     * Get the underlying NamedParameterJdbcTemplate instance
     * @return {@link NamedParameterJdbcTemplate} bean
     */
    NamedParameterJdbcTemplate getNamedJdbcTemplate();

    /**
     * Get the logger for this JDBC wrapper implementation
     * @return logger instance
     */
    Logger getLogger();

    /**
     * <p>
     *     Wrap an identifier into a standard quoted identifier, as per the dialect.
     * </p>
     * <p>
     *     Normalizes quoting across identifier parts.
     * </p>
     * @param identifier String identifier to a properly quoted identifier
     * @return quoted identifier
     */
    String wrapIdentifier(String identifier);

    /**
     * Execute an SQL statement
     * @param sql to execute
     */
    default void execute(String sql) {
        getLogger().debug(sql);
        getJdbcTemplate().execute(sql);
    }

    /**
     * Execute an SQL update (mutation) with no parameters.
     * @param sql update to run
     * @return rows affected
     */
    default int update(String sql) {
        getLogger().debug(sql);
        return getJdbcTemplate().update(sql);
    }

    /**
     * Execute an SQL update/mutation with parameters
     * @param sql SQL query string
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return number of modified rows
     */
    default int update(String sql, Object... args) {
        getLogger().debug(sql);
        var flattened = FlattenedParameters.of(sql, args);
        return getJdbcTemplate().update(flattened.sql(), flattened.toArray());
    }

    /**
     * Execute an SQL update/mutation with named parameters
     * @param sql SQL query string
     * @param namedArgs map of named arguments
     * @return number of modified rows
     */
    default int update(String sql, Map<String, ?> namedArgs) {
        getLogger().debug(sql);
        return getNamedJdbcTemplate().update(sql, namedArgs);
    }

    /**
     * Run an SQL query and collect the rows into a list
     * @param sql SQL query
     * @param rowMapper function to map each resulting row
     * @return the mapped result set
     * @param <T> type of the result
     */
    default <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        getLogger().debug(sql);
        return getJdbcTemplate().query(sql, rowMapper);
    }

    /**
     * Run an SQL query with flattened parameters and collect the rows into a list.
     * @param sql SQL query
     * @param rowMapper function to map each resulting row
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return the mapped result set
     * @param <T> type of the result
     */
    default <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        getLogger().debug(sql);
        var flattened = FlattenedParameters.of(sql, args);
        return getJdbcTemplate().query(flattened.sql(), rowMapper, flattened.toArray());
    }

    /**
     * Run an SQL query with named parameters and collect the rows into a list.
     * @param sql SQL query
     * @param rowMapper function to map each resulting row
     * @param namedArgs map of named parameters
     * @return the mapped result set
     * @param <T> type of the result
     */
    default <T> List<T> query(String sql, RowMapper<T> rowMapper, Map<String, ?> namedArgs) {
        getLogger().debug(sql);
        return getNamedJdbcTemplate().query(sql, namedArgs, rowMapper);
    }

    /**
     * Runs an SQL query without parameters and collects the result into an {@link Optional} value.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param rowMapper function to map the resulting row
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results in more than 1 row
     * @param <T> type of the Optional result
     */
    default <T> Optional<T> queryOne(String sql, RowMapper<T> rowMapper) {
        return queryOneHelper(query(sql, rowMapper));
    }

    /**
     * Runs an SQL query with flattened parameters and collects the result into an {@link Optional} value.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param rowMapper function to map the resulting row
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results in more than 1 row
     * @param <T> type of the Optional result
     */
    default <T> Optional<T> queryOne(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryOneHelper(query(sql, rowMapper, args));
    }

    /**
     * Runs an SQL query with named parameters and collects the result into an {@link Optional} value.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param rowMapper function to map the resulting row
     * @param namedArgs map of named parameters
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results in more than 1 row
     * @param <T> type of the Optional result
     */
    default <T> Optional<T> queryOne(String sql, RowMapper<T> rowMapper, Map<String, ?> namedArgs) {
        return queryOneHelper(query(sql, rowMapper, namedArgs));
    }

    /**
     * Convert a list of results into an optional value. If there are no results, an empty optional is returned.
     * If there are multiple results, an exception is thrown.
     * @param results list of row results
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results in more than 1 row
     * @param <T> type of the Optional result.
     */
    private static <T> Optional<T> queryOneHelper(List<T> results) {
        int size = results.size();
        return switch (size) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(results.getFirst());
            default -> throw new IncorrectResultSizeDataAccessException(1, size);
        };
    }

    /**
     * Query a list of 64-bit longs
     * @param sql SQL query
     * @return a list of longs
     */
    default List<Long> queryLongs(String sql) {
        getLogger().debug(sql);
        return getJdbcTemplate().queryForList(sql, Long.class);
    }

    /**
     * Query a list of 64-bit longs, with flattened parameters.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return a list of longs
     */
    default List<Long> queryLongs(String sql, Object... args) {
        getLogger().debug(sql);
        var flattened = FlattenedParameters.of(sql, args);
        return getJdbcTemplate().queryForList(flattened.sql(), Long.class, flattened.toArray());
    }

    /**
     * Query a list of 64-bit longs, with named parameters.
     * @param sql SQL query
     * @param namedArgs map of named parameters
     * @return a list of longs
     */
    default List<Long> queryLongs(String sql, Map<String, ?> namedArgs) {
        getLogger().debug(sql);
        return getNamedJdbcTemplate().queryForList(sql, namedArgs, Long.class);
    }

    /**
     * Query a list of 32-bit ints
     * @param sql SQL query
     * @return a list of integers
     */
    default List<Integer> queryInts(String sql) {
        getLogger().debug(sql);
        return getJdbcTemplate().queryForList(sql, Integer.class);
    }

    /**
     * Query a list of 32-bit ints, with flattened parameters.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return a list of integers
     */
    default List<Integer> queryInts(String sql, Object... args) {
        getLogger().debug(sql);
        var flattened = FlattenedParameters.of(sql, args);
        return getJdbcTemplate().queryForList(flattened.sql(), Integer.class, flattened.toArray());
    }

    /**
     * Query a list of 32-bit ints, with named parameters.
     * @param sql SQL query
     * @param namedArgs map of named parameters
     * @return a list of integers
     */
    default List<Integer> queryInts(String sql, Map<String, ?> namedArgs) {
        getLogger().debug(sql);
        return getNamedJdbcTemplate().queryForList(sql, namedArgs, Integer.class);
    }

    /**
     * Query a list of strings
     * @param sql SQL query
     * @return a list of strings
     */
    default List<String> queryStrings(String sql) {
        getLogger().debug(sql);
        return getJdbcTemplate().queryForList(sql, String.class);
    }

    /**
     * Query a list of strings, with flattened parameters.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return a list of integers
     */
    default List<String> queryStrings(String sql, Object... args) {
        getLogger().debug(sql);
        var flattened = FlattenedParameters.of(sql, args);
        return getJdbcTemplate().queryForList(flattened.sql(), String.class, flattened.toArray());
    }

    /**
     * Query a list of strings, with named parameters.
     * @param sql SQL query
     * @param namedArgs map of named parameters
     * @return a list of strings
     */
    default List<String> queryStrings(String sql, Map<String, ?> namedArgs) {
        getLogger().debug(sql);
        return getNamedJdbcTemplate().queryForList(sql, namedArgs, String.class);
    }

    /**
     * Run a query with no parameters for an {@link Optional} {@link Long}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<Long> queryLong(String sql) {
        return queryOneHelper(queryLongs(sql));
    }

    /**
     * Run a query with varargs parameters for an {@link Optional} {@link Long}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<Long> queryLong(String sql, Object... args) {
        return queryOneHelper(queryLongs(sql, args));
    }

    /**
     * Run a query with named parameters for an {@link Optional} {@link Long}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param namedArgs map of named parameters
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<Long> queryLong(String sql, Map<String, ?> namedArgs) {
        return queryOneHelper(queryLongs(sql, namedArgs));
    }

    /**
     * Run a query with no parameters for an {@link Optional} {@link Integer}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<Integer> queryInt(String sql) {
        return queryOneHelper(queryInts(sql));
    }

    /**
     * Run a query with varargs parameters for an {@link Optional} {@link Integer}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<Integer> queryInt(String sql, Object... args) {
        return queryOneHelper(queryInts(sql, args));
    }

    /**
     * Run a query with named parameters for an {@link Optional} {@link Integer}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param namedArgs map of named parameters
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<Integer> queryInt(String sql, Map<String, ?> namedArgs) {
        return queryOneHelper(queryInts(sql, namedArgs));
    }

    /**
     * Run a query with no parameters for an {@link Optional} {@link String}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<String> queryString(String sql) {
        return queryOneHelper(queryStrings(sql));
    }

    /**
     * Run a query with varargs parameters for an {@link Optional} {@link String}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<String> queryString(String sql, Object... args) {
        return queryOneHelper(queryStrings(sql, args));
    }

    /**
     * Run a query with named parameters for an {@link Optional} {@link String}.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param namedArgs map of named parameters
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
     */
    default Optional<String> queryString(String sql, Map<String, ?> namedArgs) {
        return queryOneHelper(queryStrings(sql, namedArgs));
    }

    /**
     * Begin a sequence of fluent API operations to define a
     * table UPDATE query, terminated (and executed) by a WHERE operation.
     * @param tableName Name of the table to update
     * @return fluent API builder to finish defining the update.
     */
    default RowUpdateBuilderSteps.First updateTable(String tableName) {
        getLogger().trace("Using [updateTable] API");
        return new RowUpdateBuilder(this, tableName);
    }

    /**
     * <p>
     *     Perform a row update query based on the given parameter object.
     * </p>
     * <p>
     *     This is not wrapped in a transaction. The user must do that themselves, if desired.
     * </p>
     * @see RowUpdate
     * @param rowUpdate {@link RowUpdate} parameter object
     * @return number of modified rows
     */
    default int update(RowUpdate rowUpdate) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(wrapIdentifier(rowUpdate.tableName()));
        sql.append(" SET ");

        StringJoiner setters = new StringJoiner(", ");
        for (var col : rowUpdate.escapedUpdates().keySet()) {
            setters.add(wrapIdentifier(col) + " = ?");
        }
        for (var entry : rowUpdate.unescapedUpdates().entrySet()) {
            setters.add(wrapIdentifier(entry.getKey()) + " = " + entry.getValue());
        }
        sql.append(setters);
        sql.append(" WHERE ");
        sql.append(rowUpdate.whereClause());

        Object[] params = rowUpdate.getParams().toArray();
        getLogger().debug(sql.toString());
        return getJdbcTemplate().update(sql.toString(), params);
    }

    /**
     * Begin a sequence of fluent API operations to define a
     * batch of UPDATE queries, terminated (and executed) by a WHERE operation.
     * One query is performed for every row in the update (rows)
     * @param tableName Name of the table to update
     * @param rows row data to map to a single update
     * @return fluent API builder to finish defining the update.
     * @param <T> type of rows to map updates on
     */
    default <T> BatchUpdateBuilderSteps.First<T> batchUpdate(String tableName, List<T> rows) {
        getLogger().trace("Using [batchUpdate] API");
        return new BatchUpdateBuilder<>(this, tableName, rows);
    }

    /**
     * <p>
     *     Perform a batch of update queries based on the given parameter object.
     * </p>
     * <p>
     *     This is not wrapped in a transaction. The user must do that themselves, if desired.
     * </p>
     * @see BatchUpdate
     * @param batchUpdate {@link BatchUpdate} parameter object
     * @param <T> type of row elements
     * @return the number of rows affected in each update
     */
    default <T> int[] update(BatchUpdate<T> batchUpdate) {
        if (batchUpdate.isEmpty()) {
            getLogger().debug("No rows in batch update");
            return new int[] {};
        }
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(wrapIdentifier(batchUpdate.tableName()));
        sql.append(" SET ");

        StringJoiner setters = new StringJoiner(", ");
        for (var col : batchUpdate.escapedMappings().keySet()) {
            setters.add(wrapIdentifier(col) + " = ?");
        }
        for (var col : batchUpdate.escapedConstants().keySet()) {
            setters.add(wrapIdentifier(col) + " = ?");
        }
        for (var entry : batchUpdate.unescapedConstants().entrySet()) {
            setters.add(wrapIdentifier(entry.getKey()) + " = " + entry.getValue());
        }
        sql.append(setters);
        sql.append(" WHERE ");
        sql.append(batchUpdate.whereClause());

        final Collection<Function<? super T, Object>> escapedMappers = batchUpdate.escapedMappings().values();
        final Collection<Object> escapedConstants = batchUpdate.escapedConstants().values();
        final int paramCount = batchUpdate.getParamCount();
        final List<Object[]> allParams = new ArrayList<>(batchUpdate.rows().size());

        for (T row : batchUpdate.rows()) {
            Object[] args = new Object[paramCount];
            int i = 0;
            for (var mapper : escapedMappers) {
                args[i++] = mapper.apply(row);
            }
            for (var constant : escapedConstants) {
                args[i++] = constant;
            }
            for (var mapper : batchUpdate.whereParamMappers()) {
                args[i++] = mapper.apply(row);
            }
            allParams.add(args);
        }
        getLogger().debug("Batch updating {} rows: {}", batchUpdate.rows().size(), sql);
        return getJdbcTemplate().batchUpdate(sql.toString(), allParams);
    }

    /**
     * Begin a sequence of fluent API operations to define a single
     * row INSERT INTO query. Terminated (and executed) by a call to insert,
     * with or without a returning a generated value.
     * @param tableName Name of the table to update
     * @return fluent API builder to finish defining the insert.
     */
    default RowInsertBuilderSteps.RequiredValue insertInto(String tableName) {
        getLogger().trace("Using [insertInto] API");
        return new RowInsertBuilder(this, tableName);
    }



    /**
     * INSERT a single row using the given {@link RowInsert} parameter object.
     * @param rowInsert configured row insert parameter
     * @return number of rows affected.
     */
    default int insert(RowInsert rowInsert) {
        String sql = InternalUtils.generateInsertSql(this, rowInsert);
        getLogger().debug(sql);
        return getJdbcTemplate().update(sql, rowInsert.escapedValues().values().toArray());
    }

    /**
     * INSERT a single row using the given {@link RowInsert} parameter object.
     * Returns a generated Number value.
     * @param rowInsert configured row insert parameter
     * @return an autogenerated {@link Number} value, nullable.
     */
    default Number insertReturningNumber(RowInsert rowInsert) {
        String sql = InternalUtils.generateInsertSql(this, rowInsert);
        getLogger().debug(sql);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = getJdbcTemplate().update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int paramIndex = 1;
            for (Object value : rowInsert.escapedValues().values()) {
                ps.setObject(paramIndex++, value);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey();
    }

    /**
     * Execute a batch insert operation and return an array of modified rows.
     * @param batchInsert batch insert configuration parameter
     * @return array of modified rows per insert.
     * @param <T> type of rows to map to inserts
     */
    default <T> int[] insert(BatchInsert<T> batchInsert) {
        if (batchInsert.rows().isEmpty()) {
            getLogger().debug("No rows in batch insert");
            return new int[] {};
        }
        String sql = InternalUtils.generateInsertSql(this, batchInsert);
        getLogger().debug("Batch inserting {} rows: {}", batchInsert.rows().size(), sql);

        Collection<Function<? super T, Object>> mappings = batchInsert.escapedMappings().values();
        Collection<Object> escapedConstants = batchInsert.escapedConstants().values();
        int paramsPerRow = mappings.size() + escapedConstants.size();
        List<Object[]> allParams = new ArrayList<>();

        for (T row : batchInsert.rows()) {
            Object[] rowParams = new Object[paramsPerRow];
            int index = 0;
            for (var fn : mappings) {
                rowParams[index++] = fn.apply(row);
            }
            for (var constant : escapedConstants) {
                rowParams[index++] = constant;
            }
            allParams.add(rowParams);
        }

        return getJdbcTemplate().batchUpdate(sql, allParams);
    }

    /**
     * Create a new fluent API builder to delete from the given table.
     * @param tableName name of the table to delete from
     * @return a fluent API builder to configure the WHERE clause
     */
    default DeleteFromBuilder deleteFrom(String tableName) {
        getLogger().trace("Using [deleteFrom] API");
        return new DeleteFromBuilder(this, tableName);
    }

    /**
     * Execute a DELETE FROM query
     * @param deleteFrom parameter type with delete configuration
     * @return number of rows affected
     */
    default int deleteFrom(DeleteFrom deleteFrom) {
        String sql = "DELETE FROM " + wrapIdentifier(deleteFrom.tableName()) +
                " WHERE " + deleteFrom.whereClause();
        getLogger().debug(sql);
        Object[] params = deleteFrom.whereParams().toArray();
        return getJdbcTemplate().update(sql, params);
    }
}
