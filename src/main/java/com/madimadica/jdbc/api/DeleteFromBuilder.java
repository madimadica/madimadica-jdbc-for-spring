package com.madimadica.jdbc.api;

import com.madimadica.jdbc.web.MadimadicaJdbc;

/**
 * Fluent API builder for a {@link DeleteFrom} parameter type.
 */
public class DeleteFromBuilder {
    private final MadimadicaJdbc jdbcImpl;
    private final String tableName;

    /**
     * Public entry point
     * @param jdbcImpl Implementation to use on terminal builder operation
     * @param tableName name of the table to insert into
     */
    public DeleteFromBuilder(MadimadicaJdbc jdbcImpl, String tableName) {
        this.jdbcImpl = jdbcImpl;
        this.tableName = tableName;
    }

    /**
     * Configure the WHERE clause for this DELETE operation and execute the deletion.
     * @param whereClause where clause template
     * @param params varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
     * @return number of rows modified
     */
    public int where(String whereClause, Object... params) {
        var whereData = FlattenedParameters.of(whereClause, params);
        var deleteFrom = new DeleteFrom(tableName, whereData.sql(), whereData.parameters());
        return jdbcImpl.deleteFrom(deleteFrom);
    }
}
