package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.FlattenedParameters;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A typed JDBC wrapper to automatically apply the instance's RowMapper
 * to queries and directly return strongly typed results.
 * @param <T> type of query results
 */
public class TypedJdbc<T> {
    protected final MadimadicaJdbc jdbc;
    protected final RowMapper<T> rowMapper;

    /**
     * Construct a non-bean instance with a given JDBC instance and the desired row mapper
     * @param jdbc jdbc implementation to use
     * @param rowMapper how to map each query result
     */
    public TypedJdbc(MadimadicaJdbc jdbc, RowMapper<T> rowMapper) {
        this.jdbc = jdbc;
        this.rowMapper = rowMapper;
    }

    /**
     * Select rows from the data source with no parameters
     * @param sql SQL query
     * @return typed rows
     */
    public List<T> query(String sql) {
        return jdbc.query(sql, rowMapper);
    }


    /**
     * Select rows from the data source with parameters
     * @param sql SQL query
     * @param params varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return typed rows
     */
    public List<T> query(String sql, Object... params) {
        return jdbc.query(sql, rowMapper, params);
    }

    /**
     * Select rows from the data source with named parameters
     * @param sql SQL query
     * @param namedParameters map of named parameters
     * @return typed rows
     */
    public List<T> query(String sql, Map<String, ?> namedParameters) {
        return jdbc.query(sql, rowMapper, namedParameters);
    }


    /**
     * Select a row from the data source with no parameters
     * @param sql SQL query
     * @return optional row
     * @throws IncorrectResultSizeDataAccessException if the query results in more than 1 row
     */
    public Optional<T> queryOne(String sql) {
        return jdbc.queryOne(sql, rowMapper);
    }


    /**
     * Select a row from the data source with parameters
     * @param sql SQL query
     * @param params varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return optional row
     * @throws IncorrectResultSizeDataAccessException if the query results in more than 1 row
     */
    public Optional<T> queryOne(String sql, Object... params) {
        return jdbc.queryOne(sql, rowMapper, params);
    }

    /**
     * Select a row from the data source with named parameters
     * @param sql SQL query
     * @param namedParameters map of named parameters
     * @return optional row
     * @throws IncorrectResultSizeDataAccessException if the query results in more than 1 row
     */
    public Optional<T> queryOne(String sql, Map<String, ?> namedParameters) {
        return jdbc.queryOne(sql, rowMapper, namedParameters);
    }

}
