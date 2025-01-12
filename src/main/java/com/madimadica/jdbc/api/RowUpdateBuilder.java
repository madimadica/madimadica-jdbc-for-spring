package com.madimadica.jdbc.api;

import com.madimadica.jdbc.web.MadimadicaJdbc;

import java.util.*;

/**
 * Fluent builder implementation for {@link RowUpdate}.
 */
public class RowUpdateBuilder implements RowUpdateBuilderSteps.Last {
    private final MadimadicaJdbc jdbcImpl;
    private final String tableName;
    private final Map<String, Object> escapedUpdates = new LinkedHashMap<>();
    private final Map<String, Object> uncapedUpdates = new LinkedHashMap<>();

    /**
     * Public entry point
     * @param jdbcImpl Implementation to use on terminal builder operation
     * @param tableName name of the table to update
     */
    public RowUpdateBuilder(MadimadicaJdbc jdbcImpl, String tableName) {
        this.jdbcImpl = jdbcImpl;
        this.tableName = tableName;
    }

    @Override
    public RowUpdateBuilderSteps.Last set(String column, Object value) {
        this.escapedUpdates.put(column, value);
        return this;
    }

    @Override
    public RowUpdateBuilderSteps.Last set(Map<String, Object> changes) {
        this.escapedUpdates.putAll(changes);
        return this;
    }

    @Override
    public RowUpdateBuilderSteps.Last setUnescaped(String column, Object value) {
        this.uncapedUpdates.put(column, value);
        return this;
    }

    @Override
    public RowUpdateBuilderSteps.Last setUnescaped(Map<String, Object> changes) {
        this.uncapedUpdates.putAll(changes);
        return this;
    }

    @Override
    public int where(String whereClause, Object... whereParams) {
        var whereData = FlattenedParameters.of(whereClause, whereParams);
        RowUpdate update = new RowUpdate(
                this.tableName,
                this.escapedUpdates,
                this.uncapedUpdates,
                whereData.sql(),
                whereData.parameters()
        );
        return jdbcImpl.update(update);
    }
}
