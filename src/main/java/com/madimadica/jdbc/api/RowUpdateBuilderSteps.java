package com.madimadica.jdbc.api;

import java.util.List;
import java.util.Map;

/**
 * Namespace container for the fluent builder steps used by {@link RowUpdateBuilder} to construct a {@link RowUpdate}.
 */
public final class RowUpdateBuilderSteps {
    /**
     * Restrict public access from constructing this namespace class
     */
    private RowUpdateBuilderSteps() {}

    /**
     * Represents the first step in configuring a row update,
     * which is specifying at least one column to set.
     * @see Last
     * @see RowUpdate
     */
    public interface First {
        /**
         * Set a column by name to an escaped value
         * @param column column name
         * @param value value to escape
         * @return {@link Last} step builder
         */
        Last set(String column, Object value);

        /**
         * Set multiple mappings from column names to values.
         * The map entry keys are the column names, and the entry values are the value to assign, as an escaped value.
         * @param changes Map of column name-value mappings
         * @return {@link Last} step builder
         */
        Last set(Map<String, Object> changes);

        /**
         * Set a column by name to an <strong>unescaped</strong> value.
         * <br>
         * This is capable of introducing SQL injection if not careful.
         * This should only be used with literal values like "GETDATE()"
         * or constants in code. Do not use this method with anything user-controlled.
         *
         * @param column column name
         * @param value value to escape
         * @return {@link Last} step builder
         */
        Last setUnescaped(String column, Object value);

        /**
         * Set multiple mappings from column names to values.
         * The map entry keys are the column names, and the entry values are the value to assign, as an <strong>unescaped</strong> value.
         * <br>
         * This is capable of introducing SQL injection if not careful.
         * This should only be used with literal values like "GETDATE()"
         * or constants in code. Do not use this method with anything user-controlled.
         * @param changes Map of column name-value mappings
         * @return {@link Last} step builder
         */
        Last setUnescaped(Map<String, Object> changes);
    }

    /**
     * Represents the last step in configuring a row update.
     * This may involve specifying additional columns to set
     * and is terminated by assigning a WHERE clause.
     * The terminal WHERE operation will execute an update statement once invoked.
     * @see First
     * @see RowUpdate
     */
    public interface Last extends First {
        /**
         * Set a WHERE clause specifying an ID for an <code>id</code> column. Completes the API builder and executes the update statement.
         * <p>
         *      <code>whereIdEquals(value)</code>  is equivalent to invoking <code>where("id = ?", value)</code>
         * </p>
         * @param id ID to check for equality, escaped
         * @return {@link int} number of rows affected
         */
        default int whereIdEquals(Object id) {
            return this.where("id = ?", id);
        }

        /**
         * Set a WHERE clause specifying IDs for an <code>id</code> column. Completes the API builder and executes the update statement.
         * <p>
         *      <code>whereIdIn(ids)</code> is equivalent to invoking <code>where("id = IN (?)", ids)</code>
         * </p>
         * @param ids list of ID values to check for containment, each escaped
         * @return {@link int} number of rows affected
         */
        default int whereIdIn(List<?> ids) {
            return this.where("id IN (?)", ids);
        }

        /**
         * <p>
         *     Set an arbitrary WHERE clause with escaped parameters. Completes the API builder and executes the update statement.
         *     using {@link com.madimadica.jdbc.web.MadimadicaJdbc#update(RowUpdate)}
         * </p>
         * <p>
         *     If you need a RowUpdate without a WHERE clause, use something like <code>where("1 = 1")</code>, or equivalent in your dialect.
         * </p>
         * @param whereClause parameterized SQL WHERE clause, excluding the <code>"WHERE"</code> keyword.
         * @param whereParams varargs parameters, which are flattened according to {@link FlattenedParameters#of(String, Object...)}
         * @throws IndexOutOfBoundsException if there are not enough varargs for the parameters.
         * @throws IllegalArgumentException if there are more varargs than parameters.
         * @return {@link int} number of rows affected
         */
        int where(String whereClause, Object... whereParams);
    }

}
