package com.madimadica.jdbc.api;

import com.madimadica.jdbc.web.JdbcRowInsertWithExplicitId;
import org.springframework.jdbc.core.RowMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder implementation for defining a single {@link RowInsert}. Can also be configured to explicitly return generated values.
 */
public class RowInsertBuilderWithExplicitId implements RowInsertBuilderStepsWithExplicitId.AdditionalValues {
    private final JdbcRowInsertWithExplicitId jdbcImpl;
    private final String tableName;
    private final Map<String, Object> escapedValues = new LinkedHashMap<>();
    private final Map<String, Object> unescapedValues = new LinkedHashMap<>();

    /**
     * Public entry point
     * @param jdbcImpl Implementation to use on terminal builder operation
     * @param tableName name of the table to insert into
     */
    public RowInsertBuilderWithExplicitId(JdbcRowInsertWithExplicitId jdbcImpl, String tableName) {
        this.jdbcImpl = jdbcImpl;
        this.tableName = tableName;
    }

    @Override
    public RowInsertBuilderStepsWithExplicitId.AdditionalValues value(String column, Object escapedValue) {
        this.escapedValues.put(column, escapedValue);
        return this;
    }

    @Override
    public RowInsertBuilderStepsWithExplicitId.AdditionalValues valueUnescaped(String column, Object unescapedValue) {
        this.unescapedValues.put(column, unescapedValue);
        return this;
    }

    /**
     * Convert the current builder configuration into a parameter object type
     * @return {@link RowInsert} parameter object
     */
    private RowInsert toParameter() {
        return new RowInsert(tableName, escapedValues, unescapedValues);
    }

    @Override
    public int insert() {
        return jdbcImpl.insert(toParameter());
    }

    @Override
    public Number insertReturningNumber(String columnName) {
        return jdbcImpl.insertReturningNumber(toParameter(), columnName);
    }

    @Override
    public <T> T insertReturning(RowMapper<T> rowMapper) {
        return jdbcImpl.insertReturning(toParameter(), rowMapper);
    }

}
