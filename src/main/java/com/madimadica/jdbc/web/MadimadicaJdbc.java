package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.BatchUpdate;
import com.madimadica.jdbc.api.FlattenedParameters;
import com.madimadica.jdbc.api.RowUpdate;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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
        getJdbcTemplate().execute(sql);
    }

    /**
     * Execute an SQL update (mutation) with no parameters.
     * @param sql update to run
     */
    default int update(String sql) {
        return getJdbcTemplate().update(sql);
    }

    /**
     * Execute an SQL update/mutation with parameters
     * @param sql SQL query string
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return number of modified rows
     */
    default int update(String sql, Object... args) {
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
        return getNamedJdbcTemplate().query(sql, namedArgs, rowMapper);
    }

    /**
     * Runs an SQL query without parameters and collects the result into an {@link Optional} value.
     * The result is empty if no rows were returned, and present if 1 row was returned.
     * @param sql SQL query
     * @param rowMapper function to map the resulting row
     * @return an optionally mapped row.
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
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
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
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
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
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
     * @throws IncorrectResultSizeDataAccessException if the query results more than 1 row
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
        return getJdbcTemplate().queryForList(sql, Long.class);
    }

    /**
     * Query a list of 64-bit longs, with flattened parameters.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return a list of longs
     */
    default List<Long> queryLongs(String sql, Object... args) {
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
        return getNamedJdbcTemplate().queryForList(sql, namedArgs, Long.class);
    }

    /**
     * Query a list of 32-bit ints
     * @param sql SQL query
     * @return a list of integers
     */
    default List<Integer> queryInts(String sql) {
        return getJdbcTemplate().queryForList(sql, Integer.class);
    }

    /**
     * Query a list of 32-bit ints, with flattened parameters.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return a list of integers
     */
    default List<Integer> queryInts(String sql, Object... args) {
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
        return getNamedJdbcTemplate().queryForList(sql, namedArgs, Integer.class);
    }

    /**
     * Query a list of strings
     * @param sql SQL query
     * @return a list of strings
     */
    default List<String> queryStrings(String sql) {
        return getJdbcTemplate().queryForList(sql, String.class);
    }

    /**
     * Query a list of strings, with flattened parameters.
     * @param sql SQL query
     * @param args varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return a list of integers
     */
    default List<String> queryStrings(String sql, Object... args) {
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
        return getNamedJdbcTemplate().queryForList(sql, namedArgs, String.class);
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
        return getJdbcTemplate().update(sql.toString(), params);
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
     */
    default <T> int[] update(BatchUpdate<T> batchUpdate) {
        if (batchUpdate.isEmpty()) {
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
        return getJdbcTemplate().batchUpdate(sql.toString(), allParams);
    }

}
