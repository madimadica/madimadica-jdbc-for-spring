package com.madimadica.jdbc.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Represents the state to perform a table row update</p>
 * <p>
 *     Can be constructed directly, or more likely constructed through the fluent API. For example:
 * </p>
 * <pre>
 * RowUpdate.of("users")
 *         .set("first_name", dto.firstName())
 *         .set("last_name", dto.lastName())
 *         .whereIdEquals(dto.id())
 * </pre>
 *
 * @param tableName name of the table to update
 * @param escapedUpdates mapping of column names to escaped values
 * @param unescapedUpdates mapping of column names to unescaped values
 * @param whereClause WHERE clause excluding the <code>"WHERE"</code>
 * @param whereParams list of parameters to bind to the WHERE clause
 */
public record RowUpdate(
        String tableName,
        Map<String, Object> escapedUpdates,
        Map<String, Object> unescapedUpdates,
        String whereClause,
        List<Object> whereParams
) {

    /**
     * Construct a valid RowUpdate parameter object.
     * @param tableName name of the table to update
     * @param escapedUpdates mapping of column names to escaped values
     * @param unescapedUpdates mapping of column names to unescaped values
     * @param whereClause WHERE clause excluding the <code>"WHERE"</code>
     * @param whereParams list of parameters to bind to the WHERE clause
     * @throws NullPointerException if any argument is <code>null</code>.
     */
    public RowUpdate {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(escapedUpdates);
        Objects.requireNonNull(unescapedUpdates);
        Objects.requireNonNull(whereClause);
        Objects.requireNonNull(whereParams);
        if (escapedUpdates.isEmpty() && unescapedUpdates.isEmpty()) {
            throw new IllegalArgumentException("At least one column update must be provided");
        }
    }

    /**
     * Get a list of the parameterized values, with the ordered updates appearing first,
     * then the where clause parameters.
     * @return ordered list of escaped parameters.
     */
    public List<Object> getParams() {
        List<Object> params = new ArrayList<>(this.escapedUpdates.values());
        params.addAll(whereParams);
        return params;
    }

}
