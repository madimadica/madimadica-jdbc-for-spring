package com.madimadica.jdbc.api;

import java.util.List;
import java.util.function.Function;

/**
 * Namespace container for the fluent builder steps used by {@link BatchUpdateBuilder} to construct a {@link BatchUpdate}.
 */
public class BatchUpdateBuilderSteps {
    /**
     * Restrict public access from constructing this namespace class
     */
    private BatchUpdateBuilderSteps() {}

    /**
     * Represents the first step in configuring a batch update,
     * which is specifying at least one column to set.
     * @see BatchUpdateBuilderSteps.Last
     * @see BatchUpdate
     * @param <T> type of row elements
     */
    public interface First<T> {
        /**
         * Set a column by name to a functionally mapped value, escaped
         * @param column column name
         * @param valueMapper how to get the current value
         * @return {@link BatchUpdateBuilderSteps.Last} step builder
         */
        Last<T> set(String column, Function<? super T, Object> valueMapper);

        /**
         * Set a column by name to a constant value, escaped
         * @param column column name
         * @param constant constant value to escape
         * @return {@link BatchUpdateBuilderSteps.Last} step builder
         */
        Last<T> set(String column, Object constant);

        /**
         * Set a column by name to a <strong>unescaped</strong> constant value
         * <p>
         * This is capable of introducing SQL injection if not careful.
         * This should only be used with literal values like "GETDATE()"
         * or constants in code. Do not use this method with anything user-controlled.
         * </p>
         * @param column column name
         * @param unescapedConstant constant value to escape
         * @return {@link BatchUpdateBuilderSteps.Last} step builder
         */
        Last<T> setUnescaped(String column, Object unescapedConstant);
    }

    /**
     * Represents the last step in configuring a batch update,
     * which can involve setting additional column mappings, and
     * is terminated by assigning a WHERE clause. The terminal
     * operation executes an UPDATE statement when invoked.
     * @see BatchUpdateBuilderSteps.Last
     * @see BatchUpdate
     * @param <T> type of row elements
     */
    public interface Last<T> extends First<T> {
        /**
         * Set a WHERE clause with functional mappings to escaped parameters.
         * Performs terminal UPDATE query operation.
         * @param whereClause where clause template
         * @param whereMappers functions to map <code>T</code> to parameters
         * @return number of rows affected per query
         */
        int[] where(String whereClause, List<Function<? super T, Object>> whereMappers);

        /**
         * Set a WHERE clause to an <code>id</code> column check, mapped by the given ID function.
         * Performs terminal UPDATE query operation.
         * <p>
         *      <code>whereIdEquals(Foo::bar)</code> is equivalent to invoking <code>where("id = ?", Foo::bar)</code>
         * </p>
         * @param idMapper functions to map <code>T</code> to the ID
         * @see Last#where(String, List)
         * @return number of rows affected per query
         */
        default int[] whereIdEquals(Function<? super T, Object> idMapper) {
            return where("id = ?", idMapper);
        }

        /**
         * <p>
         *     An overloaded method to assign a WHERE clause mapping and execute an UPDATE
         * </p>
         * @param whereClause where clause template
         * @param whereMapper function to map <code>T</code> to the first parameter
         * @see Last#where(String, List)
         * @return number of rows affected per query
         */
        default int[] where(String whereClause, Function<? super T, Object> whereMapper) {
            return where(whereClause, List.of(whereMapper));
        }

        /**
         * <p>
         *     An overloaded method to assign a WHERE clause mapping and execute an UPDATE
         * </p>
         * @param whereClause where clause template
         * @param whereMapper1 function to map <code>T</code> to the first parameter
         * @param whereMapper2 function to map <code>T</code> to the second parameter
         * @see Last#where(String, List)
         * @return number of rows affected per query
         */
        default int[] where(String whereClause, Function<? super T, Object> whereMapper1, Function<? super T, Object> whereMapper2) {
            return where(whereClause, List.of(whereMapper1, whereMapper2));
        }

        /**
         * <p>
         *     An overloaded method to assign a WHERE clause mapping and execute an UPDATE
         * </p>
         * @param whereClause where clause template
         * @param whereMapper1 function to map <code>T</code> to the first parameter
         * @param whereMapper2 function to map <code>T</code> to the second parameter
         * @param whereMapper3 function to map <code>T</code> to the third parameter
         * @see Last#where(String, List)
         * @return number of rows affected per query
         */
        default int[] where(String whereClause, Function<? super T, Object> whereMapper1, Function<? super T, Object> whereMapper2, Function<? super T, Object> whereMapper3) {
            return where(whereClause, List.of(whereMapper1, whereMapper2, whereMapper3));
        }

        /**
         * <p>
         *     An overloaded method to assign a WHERE clause mapping and execute an UPDATE
         * </p>
         * @param whereClause where clause template
         * @param whereMapper1 function to map <code>T</code> to the first parameter
         * @param whereMapper2 function to map <code>T</code> to the second parameter
         * @param whereMapper3 function to map <code>T</code> to the third parameter
         * @param whereMapper4 function to map <code>T</code> to the fourth parameter
         * @see Last#where(String, List)
         * @return number of rows affected per query
         */
        default int[] where(String whereClause, Function<? super T, Object> whereMapper1, Function<? super T, Object> whereMapper2, Function<? super T, Object> whereMapper3, Function<? super T, Object> whereMapper4) {
            return where(whereClause, List.of(whereMapper1, whereMapper2, whereMapper3, whereMapper4));
        }
    }

}
