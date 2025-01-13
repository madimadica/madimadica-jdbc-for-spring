package com.madimadica.jdbc.api;

import java.util.List;
import java.util.Objects;

/**
 * <p>Represents the state needed to perform a table DELETE FROM operation</p>
 * @param tableName name of the table to delete from
 * @param whereClause WHERE clause excluding the <code>"WHERE"</code>
 * @param whereParams list of parameters to bind to the WHERE clause
 */
public record DeleteFrom(
        String tableName,
        String whereClause,
        List<Object> whereParams
) {
    /**
     * Construct a valid parameter type
     * @param tableName name of the table to delete from
     * @param whereClause WHERE clause excluding the <code>"WHERE"</code>
     * @param whereParams list of parameters to bind to the WHERE clause
     */
    public DeleteFrom {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(whereClause);
        Objects.requireNonNull(whereParams);
    }
}
