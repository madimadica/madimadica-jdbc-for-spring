package com.madimadica.jdbc.api;

import java.util.List;
import java.util.function.Function;

/**
 * Namespace container for the fluent builder steps used by {@link BatchInsertBuilderWithImplicitId} to construct a {@link BatchInsert}.
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
     * @param <NEXT> The next fluent interface type to use
     */
    public interface RequiredValue<T, NEXT extends RequiredValue<T, NEXT>> {
        /**
         * Assign a column an escaped constant value
         * @param column name of the column to assign the value to
         * @param escapedConstant escaped constant value to set for all row inserts.
         * @return the last step in the fluent builder API
         */
        NEXT value(String column, Object escapedConstant);

        /**
         * Assign a column an <strong>un-escaped</strong> constant value
         * <p>
         *     <strong>Warning:</strong> this can introduce <em>SQL Injection</em> if used improperly.
         *     This method should <em>only</em> be used with hardcoded constants, such as "NOW()".
         * </p>
         * @param column name of the column to assign the value to
         * @param unescapedConstant unescaped constant value to set for all row inserts.
         * @return the last step in the fluent builder API
         */
        NEXT valueUnescaped(String column, Object unescapedConstant);

        /**
         * Assign a column to an escaped mapping from row data to value
         * @param column name of the column to assign the value to
         * @param valueMapper function to apply to get the value for an arbitrary row
         * @return the last step in the fluent builder API
         */
        NEXT value(String column, Function<? super T, Object> valueMapper);
    }

    /**
     * The last step in the fluent builder API.
     * Can assign as many additional values as needed,
     * and terminated by a final insert invocation that
     * executes the update.
     * Associated with databases that can implicitly return generated ID for each row on batch inserts.
     * @param <T> type of rows to insert
     */
    public interface AdditionalValuesWithImplicitID<T> extends RequiredValue<T, AdditionalValuesWithImplicitID<T>> {
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

    /**
     * The last step in the fluent builder API.
     * Can assign as many additional values as needed,
     * and terminated by a final insert invocation that
     * executes the update.
     * Associated with databases that must explicitly handle gathering the generated IDs for each row on batch inserts.
     * @param <T> type of rows to insert
     */
    public interface AdditionalValuesWithExplicitID<T> extends RequiredValue<T, AdditionalValuesWithExplicitID<T>> {
        /**
         * Execute an INSERT query for each row as a batch.
         * @return the number of rows affected by each insert.
         */
        int[] insert();

        /**
         * Execute an INSERT query for each row as a batch. Returns generated numbers.
         * @param generatedColumn the column to select generated values from
         * @return List of generated numbers, one for each insert
         */
        List<Number> insertReturningNumbers(String generatedColumn);

        /**
         * Execute an INSERT query for each row as a batch. Returns generated ints.
         * @param generatedColumn the column to select generated values from
         * @return List of generated ints, one for each insert
         */
        default List<Integer> insertReturningInts(String generatedColumn) {
            return insertReturningNumbers(generatedColumn).stream().map(Number::intValue).toList();
        }

        /**
         * Execute an INSERT query for each row as a batch. Returns generated longs.
         * @param generatedColumn the column to select generated values from
         * @return List of generated longs, one for each insert
         */
        default List<Long> insertReturningLongs(String generatedColumn) {
            return insertReturningNumbers(generatedColumn).stream().map(Number::longValue).toList();
        }
    }

}
