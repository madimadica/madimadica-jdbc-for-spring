package com.madimadica.jdbc.api;

import java.util.*;

/**
 * Fluent builder implementation for {@link RowUpdate}.
 */
public class RowUpdateBuilder implements RowUpdateBuilderSteps.Last {
    private final String tableName;
    private final Map<String, Object> escapedUpdates = new LinkedHashMap<>();
    private final Map<String, Object> uncapedUpdates = new LinkedHashMap<>();

    /**
     * Public entry point
     * @param tableName name of the table to update
     */
    public RowUpdateBuilder(String tableName) {
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
    public RowUpdate where(String whereClause, Object... whereParams) {
        var whereData = ParameterizedWhere.of(whereClause, whereParams);
        return new RowUpdate(
                this.tableName,
                this.escapedUpdates,
                this.uncapedUpdates,
                whereData.parameterizedClause(),
                whereData.parameters()
        );
    }
}
