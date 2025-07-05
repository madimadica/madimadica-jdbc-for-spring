package com.madimadica.jdbc.api;

import org.springframework.jdbc.core.RowMapper;

/**
 * Namespace container for the fluent builder steps used by {@link RowInsertBuilderWithExplicitId} to construct a {@link RowInsert}.
 * Similar to {@link RowInsertBuilderStepsWithImplicitId} but requires a column name for generated values.
 */
public class RowInsertBuilderStepsWithExplicitId {

    /**
     * Prevent user construction
     */
    private RowInsertBuilderStepsWithExplicitId() {}

    /**
     * A required step in the fluent builder API
     * to require at least a single value binding
     * before executing the insert.
     */
    public interface RequiredValue {
        /**
         * Assign a column an escaped value
         * @param column column to set an inserted value for
         * @param escapedValue value to assign to that column
         * @return {@link AdditionalValues} builder API to add additional columns or execute the insert.
         */
        AdditionalValues value(String column, Object escapedValue);

        /**
         * Assign a column an <strong>un-escaped</strong> value
         * <p>
         *     <strong>Warning:</strong> this can introduce <em>SQL Injection</em> if used improperly.
         *     This method should <em>only</em> be used with hardcoded constants, such as "NOW()".
         * </p>
         * @param column column to set an inserted value for
         * @param unescapedValue value to assign to that column
         * @return {@link AdditionalValues} builder API to add additional columns or execute the insert.
         */
        AdditionalValues valueUnescaped(String column, Object unescapedValue);
    }

    /**
     * A repeatable step in the fluent builder API to add additional
     * column/value bindings before executing the insert.
     */
    public interface AdditionalValues extends RequiredValue {
        /**
         * Execute an INSERT query for the configured row insert.
         * @return the number of rows affected by the insert, which should be 1.
         */
        int insert();

        /**
         * Execute an INSERT query for the configured row insert and returns an auto-generated int.
         * @param columnName name of the generated number's column
         * @return the auto-generated int value.
         */
        Number insertReturningNumber(String columnName);

        /**
         * Execute an INSERT query for the configured row insert and maps the row with the given rowMapper.
         * @param rowMapper how to map the inserted row
         * @return the inserted row.
         */
        <T> T insertReturning(RowMapper<T> rowMapper);

        /**
         * Execute an INSERT query for the configured row insert and returns an auto-generated int.
         * @param columnName name of the generated int's column
         * @return the auto-generated int value.
         */
        default int insertReturningInt(String columnName) {
            return insertReturningNumber(columnName).intValue();
        }

        /**
         * Execute an INSERT query for the configured row insert and returns an auto-generated long.
         * @param columnName name of the generated long's column
         * @return the auto-generated long value.
         */
        default long insertReturningLong(String columnName) {
            return insertReturningNumber(columnName).longValue();
        }
    }
}
