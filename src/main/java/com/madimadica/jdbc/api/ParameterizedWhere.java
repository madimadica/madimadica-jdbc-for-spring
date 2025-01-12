package com.madimadica.jdbc.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Internal data structure representing a parameterized WHERE clause with no functional bindings.
 *
 * @param parameterizedClause parameterized WHERE clause, excluding the "WHERE"
 * @param parameters List of parameter objects to bind to the WHERE clause
 */
record ParameterizedWhere(
        String parameterizedClause,
        List<Object> parameters
) {
    /**
     * <p>
     *     Parse an arbitrary WHERE clause with escaped parameters.
     * </p>
     * <p>
     *     To include a arbitrary length collection of parameters, such as <code>IN (?, ?, ?)</code>,
     *     use <code>"IN (?)"</code> where the parameter varargs at the corresponding index is a {@link java.util.Collection}.
     *     Then, the single <code>?</code> will be replaced by the correct length of parameters.
     * </p>
     * <p>
     *     For example, <pre>of("id IN (?) OR flag = ?", List.of(1, 2, 3), hasFlag)</pre> will be converted into
     *     the WHERE clause <pre>"id IN (?, ?, ?) OR flag = ?"</pre> and the parameters will be flattened into <code>1, 2, 3, hasFlag</code>.
     * </p>
     * <p>
     *    Collections must still correspond to exactly 1 parameter <code>?</code> mark.
     *    If there are more parameters (<code>?</code>) in the where Clause than varargs an exception will be thrown.
     *    Too few varargs will throw an {@link IndexOutOfBoundsException}, and
     *    too many will throw an {@link IllegalArgumentException}.
     * </p>
     *
     * @param whereClause parameterized where clause
     * @param whereParams varargs parameters that may contain expandable collections
     * @throws IndexOutOfBoundsException if there are not enough varargs for the parameters.
     * @throws IllegalArgumentException if there are more varargs than parameters.
     * @return {@link ParameterizedWhere} parsed data of potentially expanded parameter collections.
     */
    static ParameterizedWhere of(String whereClause, Object... whereParams) {
        List<Object> params = List.of(whereParams);
        int paramIndex = 0;
        List<Object> flattenedParams = new ArrayList<>();
        StringBuilder whereBuilder = new StringBuilder();
        int lastStart = 0;
        final int len = whereClause.length();
        for (int i = 0; i < len; ++i) {
            if (whereClause.charAt(i) == '?') {
                Object currentParam = params.get(paramIndex++);
                if (currentParam instanceof Collection<?> collection) {
                    whereBuilder.append(whereClause, lastStart, i);
                    whereBuilder.append(InternalSqlUtils.getParameters(collection.size()));
                    lastStart = i + 1;
                    flattenedParams.addAll(collection);
                } else {
                    flattenedParams.add(currentParam);
                }
            }
        }
        if (paramIndex != whereParams.length) {
            throw new IllegalArgumentException("Found " + paramIndex + " parameters but received " + whereParams.length);
        }
        whereBuilder.append(whereClause, lastStart, len);
        return new ParameterizedWhere(whereBuilder.toString(), flattenedParams);
    }
}
