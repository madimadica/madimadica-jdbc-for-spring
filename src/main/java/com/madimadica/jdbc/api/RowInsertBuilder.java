package com.madimadica.jdbc.api;

import com.madimadica.jdbc.web.MadimadicaJdbc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder implementation for defining a single {@link RowInsert}.
 */
public class RowInsertBuilder implements RowInsertBuilderSteps.AdditionalValues {
    private final MadimadicaJdbc jdbcImpl;
    private final String tableName;
    private final Map<String, Object> escapedValues = new LinkedHashMap<>();
    private final Map<String, Object> unescapedValues = new LinkedHashMap<>();

    /**
     * Public entry point
     * @param jdbcImpl Implementation to use on terminal builder operation
     * @param tableName name of the table to insert into
     */
    public RowInsertBuilder(MadimadicaJdbc jdbcImpl, String tableName) {
        this.jdbcImpl = jdbcImpl;
        this.tableName = tableName;
    }

    @Override
    public RowInsertBuilderSteps.AdditionalValues value(String column, Object escapedValue) {
        this.escapedValues.put(column, escapedValue);
        return this;
    }

    @Override
    public RowInsertBuilderSteps.AdditionalValues valueUnescaped(String column, Object unescapedValue) {
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
