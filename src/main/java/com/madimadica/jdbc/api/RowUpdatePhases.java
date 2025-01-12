package com.madimadica.jdbc.api;

import java.util.List;
import java.util.Map;

/**
 * Namespace container for the fluent builder steps used by {@link RowUpdateBuilder} to construct a {@link RowUpdate}.
 */
public final class RowUpdatePhases {
    /**
     * Restrict public access from constructing this namespace class
     */
    private RowUpdatePhases() {}

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
         * @return {@link Last} phase builder
         */
        Last set(String column, Object value);

        /**
         * Set multiple mappings from column names to values.
         * The map entry keys are the column names, and the entry values are the value to assign, as an escaped value.
         * @param changes Map of column name-value mappings
         * @return {@link Last} phase builder
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
         * @return {@link Last} phase builder
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
         * @return {@link Last} phase builder
         */
        Last setUnescaped(Map<String, Object> changes);
    }

    /**
     * Represents the last step in configuring a row update.
     * This may involve specifying additional columns to set
     * and is terminated by assigning a WHERE clause.
     * @see First
     * @see RowUpdate
     */
    public interface Last extends First {
        /**
         * Set a WHERE clause specifying an ID for an <code>id</code> column.
         * <p>
         *      <code>whereIdEquals(value)</code>  is equivalent to invoking <code>where("id = ?", value)</code>
         * </p>
         * @param id ID to check for equality, escaped
         * @return {@link RowUpdate} to complete the fluent builder API pattern
         */
        default RowUpdate whereIdEquals(Object id) {
            return this.where("id = ?", id);
        }

        /**
         * Set a WHERE clause specifying IDs for an <code>id</code> column.
         * <p>
         *      <code>whereIdIn(ids)</code> is equivalent to invoking <code>where("id = IN (?)", ids)</code>
         * </p>
         * @param ids list of ID values to check for containment, each escaped
         * @return {@link RowUpdate} to complete the fluent builder API pattern
         */
        default RowUpdate whereIdIn(List<?> ids) {
            return this.where("id IN (?)", ids);
        }

        /**
         * <p>
         *     Set an arbitrary WHERE clause with escaped parameters.
         * </p>
         * <p>
         *     To include a arbitrary length collection of parameters, such as <code>IN (?, ?, ?)</code>,
         *     use <code>"IN (?)"</code> where the parameter varargs at the corresponding index is a {@link java.util.Collection}.
         *     Then, the single <code>?</code> will be replaced by the correct length of parameters.
         * </p>
         * <p>
         *     For example,
         * </p>
         * <pre>where("id IN (?) OR flag = ?", List.of(1, 2, 3), hasFlag)</pre>
         * <p>will be converted into
         *     the WHERE clause <code>"id IN (?, ?, ?) OR flag = ?"</code> and
         *     the parameters will be flattened into <code>1, 2, 3, hasFlag</code>.
         * </p>
         * <p>
         *    Collections must still correspond to exactly 1 parameter <code>?</code> mark.
         *    If there are more parameters (<code>?</code>) in the where Clause than varargs an exception will be thrown.
         *    Too few varargs will throw an {@link IndexOutOfBoundsException}, and
         *    too many will throw an {@link IllegalArgumentException}.
         * </p>
         * <p>
         *     If you need a RowUpdate without a WHERE clause, use something like <code>where("1 = 1")</code>, or equivalent in your dialect.
         * </p>
         * @param whereClause parameterized SQL WHERE clause, excluding the <code>"WHERE"</code> keyword.
         * @param whereParams varargs list of parameters to use and escape with the WHERE clause.
         *                    May contain collections that automatically expand into the correct arguments.
         *                    The length of this <em>must</em> match the number of
         *                    <code>?</code> parameters in the <code>whereClause</code>.
         * @throws IndexOutOfBoundsException if there are not enough varargs for the parameters.
         * @throws IllegalArgumentException if there are more varargs than parameters.
         * @return {@link RowUpdate} to complete the fluent builder API pattern
         */
        RowUpdate where(String whereClause, Object... whereParams);
    }

}
