package com.madimadica.jdbc.api;

import com.madimadica.jdbc.web.JdbcRowInsertWithImplicitId;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder implementation for defining a single {@link RowInsert}.
 */
public class RowInsertBuilderWithImplicitId implements RowInsertBuilderStepsWithImplicitId.AdditionalValues {
    private final JdbcRowInsertWithImplicitId jdbcImpl;
    private final String tableName;
    private final Map<String, Object> escapedValues = new LinkedHashMap<>();
    private final Map<String, Object> unescapedValues = new LinkedHashMap<>();

    /**
     * Public entry point
     * @param jdbcImpl Implementation to use on terminal builder operation
     * @param tableName name of the table to insert into
     */
    public RowInsertBuilderWithImplicitId(JdbcRowInsertWithImplicitId jdbcImpl, String tableName) {
        this.jdbcImpl = jdbcImpl;
        this.tableName = tableName;
    }

    @Override
    public RowInsertBuilderStepsWithImplicitId.AdditionalValues value(String column, Object escapedValue) {
        this.escapedValues.put(column, escapedValue);
        return this;
    }

    @Override
    public RowInsertBuilderStepsWithImplicitId.AdditionalValues valueUnescaped(String column, Object unescapedValue) {
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
    public Number insertReturningNumber() {
        return jdbcImpl.insertReturningNumber(toParameter());
    }

}
