package com.madimadica.jdbc.api;

import com.madimadica.jdbc.web.MadimadicaJdbc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Fluent builder implementation for {@link BatchUpdate}
 * @param <T> Type of list elements to create updates for
 */
public class BatchUpdateBuilder<T> implements BatchUpdateBuilderSteps.Last<T> {

    private final MadimadicaJdbc jdbcImpl;
    private final String tableName;
    private final List<T> rows;
    private final Map<String, Function<? super T, Object>> escapedMappings = new LinkedHashMap<>();
    private final Map<String, Object> escapedConstants = new LinkedHashMap<>();
    private final Map<String, Object> unescapedConstants = new LinkedHashMap<>();

    /**
     * Public entry point
     * @param jdbcImpl Implementation to use on terminal builder operation
     * @param tableName name of the table to update
     * @param rows rows reference to use for generating updates
     */
    public BatchUpdateBuilder(MadimadicaJdbc jdbcImpl, String tableName, List<T> rows) {
        this.jdbcImpl = jdbcImpl;
        this.tableName = tableName;
        this.rows = rows;
    }

    @Override
    public BatchUpdateBuilderSteps.Last<T> set(String column, Function<? super T, Object> valueMapper) {
        this.escapedMappings.put(column, valueMapper);
        return this;
    }

    @Override
    public BatchUpdateBuilderSteps.Last<T> set(String column, Object constant) {
        this.escapedConstants.put(column, constant);
        return this;
    }

    @Override
    public BatchUpdateBuilderSteps.Last<T> setUnescaped(String column, Object unescapedConstant) {
        this.unescapedConstants.put(column, unescapedConstant);
        return this;
    }

    @Override
    public int[] where(String whereClause, List<Function<? super T, Object>> whereMappers) {
        BatchUpdate<T> batchUpdate = new BatchUpdate<>(
                tableName,
                rows,
                escapedMappings,
                escapedConstants,
                unescapedConstants,
                whereClause,
                whereMappers
        );
        return jdbcImpl.update(batchUpdate);
    }
}
