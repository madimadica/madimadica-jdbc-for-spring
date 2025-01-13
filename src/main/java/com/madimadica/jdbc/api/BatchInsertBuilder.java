package com.madimadica.jdbc.api;

import com.madimadica.jdbc.web.MadimadicaJdbc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Fluent builder implementation for defining a {@link BatchInsert}.
 * @param <T> type of the rows to insert in this batch
 */
public class BatchInsertBuilder<T> implements BatchInsertBuilderSteps.AdditionalValues<T> {
    private final MadimadicaJdbc jdbcImpl;
    private final String tableName;
    private final List<T> rows;
    private final Map<String, Function<? super T, Object>> escapedMappings = new LinkedHashMap<>();
    private final Map<String, Object> escapedConstants = new LinkedHashMap<>();
    private final Map<String, Object> unescapedConstants = new LinkedHashMap<>();

    /**
     * Public entry point
     * @param jdbcImpl Implementation to use on terminal builder operation
     * @param tableName name of the table to insert into
     * @param rows list of rows to insert
     */
    public BatchInsertBuilder(MadimadicaJdbc jdbcImpl, String tableName, List<T> rows) {
        this.jdbcImpl = jdbcImpl;
        this.tableName = tableName;
        this.rows = rows;
    }

    @Override
    public BatchInsertBuilderSteps.AdditionalValues<T> value(String column, Object escapedConstant) {
        this.escapedConstants.put(column, escapedConstant);
        return this;
    }

    @Override
    public BatchInsertBuilderSteps.AdditionalValues<T> valueUnescaped(String column, Object unescapedConstant) {
        this.unescapedConstants.put(column, unescapedConstant);
        return this;
    }

    @Override
    public BatchInsertBuilderSteps.AdditionalValues<T> value(String column, Function<? super T, Object> valueMapper) {
        this.escapedMappings.put(column, valueMapper);
        return this;
    }

    private BatchInsert<T> toParameter() {
        return new BatchInsert<>(
                tableName,
                rows,
                escapedMappings,
                escapedConstants,
                unescapedConstants
        );
    }

    @Override
    public int[] insert() {
        return jdbcImpl.insert(toParameter());
    }

    @Override
    public List<Number> insertReturningNumbers() {
        return jdbcImpl.insertReturningNumbers(toParameter());
    }
}
