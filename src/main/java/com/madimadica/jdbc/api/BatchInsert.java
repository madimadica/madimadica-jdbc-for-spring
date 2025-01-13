package com.madimadica.jdbc.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * <p>Represents the state to perform a batch of inserts to a single table</p>
 *
 * @param tableName name of the table to INSERT INTO
 * @param rows row objects to insert
 * @param escapedMappings mappings to escaped values for each T
 * @param escapedConstants constant values for all T, escaped
 * @param unescapedConstants constant values for all T, unescaped
 * @param <T> Type of each list element to map into an inserted row
 */
public record BatchInsert<T> (
        String tableName,
        List<T> rows,
        Map<String, Function<? super T, Object>> escapedMappings,
        Map<String, Object> escapedConstants,
        Map<String, Object> unescapedConstants
) {
    /**
     * Construct a valid batch of inserts
     * @param tableName name of the table to INSERT INTO
     * @param rows row objects to insert
     * @param escapedMappings mappings to escaped values for each T
     * @param escapedConstants constant values for all T, escaped
     * @param unescapedConstants constant values for all T, unescaped
     */
    public BatchInsert {
        if (escapedMappings.isEmpty() && escapedConstants.isEmpty() && unescapedConstants.isEmpty()) {
            throw new IllegalArgumentException("Must insert at least one column");
        }
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(rows);
    }
}
