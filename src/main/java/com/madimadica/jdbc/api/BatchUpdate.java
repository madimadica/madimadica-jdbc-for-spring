package com.madimadica.jdbc.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>Represents the state to perform a batch of updates to a single table</p>
 * <p>
 *     If you need an escaped constant in the WHERE clause parameters,
 *     use a lambda such as <code>ignored -> myConstant</code> for the function.
 * </p>
 * @param tableName name of the table to update
 * @param rows objects to map row updates for
 * @param escapedMappings mappings to escaped values for each T
 * @param escapedConstants constant values for all T, escaped
 * @param unescapedConstants constant values for all T, unescaped
 * @param whereClause WHERE clause template for all T
 * @param whereParamMappers WHERE clause parameter mappers for each T
 * @param <T> Type of each list element to map to column values
 */
public record BatchUpdate<T> (
    String tableName,
    List<T> rows,
    Map<String, Function<? super T, Object>> escapedMappings,
    Map<String, Object> escapedConstants,
    Map<String, Object> unescapedConstants,
    String whereClause,
    List<Function<? super T, Object>> whereParamMappers
) {
    /**
     * Construct a well-formed, valid BatchUpdate parameter object.
     * @param tableName name of the table to update
     * @param rows objects to map row updates for
     * @param escapedMappings mappings to escaped values for each T
     * @param escapedConstants constant values for all T, escaped
     * @param unescapedConstants constant values for all T, unescaped
     * @param whereClause WHERE clause template for all T
     * @param whereParamMappers WHERE clause parameter mappers for each T
     */
    public BatchUpdate {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(rows);
        Objects.requireNonNull(escapedMappings);
        Objects.requireNonNull(escapedConstants);
        Objects.requireNonNull(unescapedConstants);
        Objects.requireNonNull(whereClause);
        Objects.requireNonNull(whereParamMappers);
        if (escapedMappings.isEmpty() && escapedConstants.isEmpty() && unescapedConstants.isEmpty()) {
            throw new IllegalArgumentException("Must update at least one column");
        }
    }

    /**
     * Get the number of parameters (?) per row
     * @return number of parameters per row
     */
    public int getParamCount() {
        return escapedMappings.size() + escapedConstants.size() + whereParamMappers.size();
    }

    /**
     * Returns <code>true</code> if there are no row elements in this batch update.
     * @return <code>true</code> if there are no row elements in this batch update.
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }
}