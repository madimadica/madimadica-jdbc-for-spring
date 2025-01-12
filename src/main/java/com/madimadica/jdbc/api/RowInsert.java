package com.madimadica.jdbc.api;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the state needed to perform an INSERT INTO operation.
 * Used as a parameter object.
 * @param tableName name of the table to INSERT INTO
 * @param escapedValues mapping of column names to escaped values
 * @param unescapedValues mapping of column names to unescaped values
 */
public record RowInsert(
        String tableName,
        Map<String, Object> escapedValues,
        Map<String, Object> unescapedValues
) {
    /**
     * Construct a valid object
     * @param tableName name of the table to INSERT INTO
     * @param escapedValues mapping of column names to escaped values
     * @param unescapedValues mapping of column names to unescaped values
     */
    public RowInsert {
        if (escapedValues.isEmpty() && unescapedValues.isEmpty()) {
            throw new IllegalArgumentException("Row insertion must have at least one value");
        }
        Objects.requireNonNull(tableName);
    }
}
