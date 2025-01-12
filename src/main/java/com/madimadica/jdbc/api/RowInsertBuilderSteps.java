package com.madimadica.jdbc.api;

/**
 * Namespace container for the fluent builder steps used by {@link RowInsertBuilder} to construct a {@link RowInsert}.
 */
public class RowInsertBuilderSteps {

    /**
     * Prevent user construction
     */
    private RowInsertBuilderSteps() {}

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
         * @return the auto-generated int value.
         */
        Number insertReturningNumber();

        /**
         * Execute an INSERT query for the configured row insert and returns an auto-generated int.
         * @return the auto-generated int value.
         */
        default int insertReturningInt() {
            return insertReturningNumber().intValue();
        }

        /**
         * Execute an INSERT query for the configured row insert and returns an auto-generated long.
         * @return the auto-generated long value.
         */
        default long insertReturningLong() {
            return insertReturningNumber().longValue();
        }
    }
}
