package com.madimadica.jdbc.api;

import java.util.List;
import java.util.function.Function;

/**
 * Namespace container for the fluent builder steps used by {@link BatchInsertBuilder} to construct a {@link BatchInsert}.
 */
public class BatchInsertBuilderSteps {
    /**
     * Prevent user instantiation;
     */
    private BatchInsertBuilderSteps() {}

    /**
     * Represents the first step in the fluent builder API for a batch insert.
     * Requires adding at least one column/value pair to the model.
     * @param <T> Type of the row elements to map
     */
    public interface RequiredValue<T> {
        /**
         * Assign a column an escaped constant value
         * @param column name of the column to assign the value to
         * @param escapedConstant escaped constant value to set for all row inserts.
         * @return {@link AdditionalValues} last step in the fluent builder API
         */
        AdditionalValues<T> value(String column, Object escapedConstant);

        /**
         * Assign a column an <strong>un-escaped</strong> constant value
         * <p>
         *     <strong>Warning:</strong> this can introduce <em>SQL Injection</em> if used improperly.
         *     This method should <em>only</em> be used with hardcoded constants, such as "NOW()".
         * </p>
         * @param column name of the column to assign the value to
         * @param unescapedConstant unescaped constant value to set for all row inserts.
         * @return {@link AdditionalValues} last step in the fluent builder API
         */
        AdditionalValues<T> valueUnescaped(String column, Object unescapedConstant);

        /**
         * Assign a column to an escaped mapping from row data to value
         * @param column name of the column to assign the value to
         * @param valueMapper function to apply to get the value for an arbitrary row
         * @return {@link AdditionalValues} last step in the fluent builder API
         */
        AdditionalValues<T> value(String column, Function<? super T, Object> valueMapper);
    }

    /**
     * The last step in the fluent builder API.
     * Can assign as many additional values as needed,
     * and terminated by a final insert invocation that
     * executes the update.
     * @param <T> type of rows to insert
     */
    public interface AdditionalValues<T> extends RequiredValue<T> {
        /**
         * Execute an INSERT query for each row as a batch.
         * @return the number of rows affected by each insert.
         */
        int[] insert();

        /**
         * Execute an INSERT query for each row as a batch. Returns generated numbers.
         * @return List of generated numbers, one for each insert
         */
        List<Number> insertReturningNumbers();

        /**
         * Execute an INSERT query for each row as a batch. Returns generated ints.
         * @return List of generated ints, one for each insert
         */
        default List<Integer> insertReturningInts() {
            return insertReturningNumbers().stream().map(Number::intValue).toList();
        }

        /**
         * Execute an INSERT query for each row as a batch. Returns generated longs.
         * @return List of generated longs, one for each insert
         */
        default List<Long> insertReturningLongs() {
            return insertReturningNumbers().stream().map(Number::longValue).toList();
        }
    }

}
