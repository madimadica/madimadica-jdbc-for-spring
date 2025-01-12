package com.madimadica.jdbc.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Internal data structure representing a parameterized SQL string
 * and flattening and normalizing input varargs.
 *
 * @param sql parameterized SQL
 * @param parameters List of parameters
 */
public record FlattenedParameters(
        String sql,
        List<Object> parameters
) {

    /**
     * Return the parameters as an <code>Object[]</code>
     * @return an <code>Object[]</code> of parameters
     */
    public Object[] toArray() {
        return parameters.toArray();
    }

    /**
     * <p>
     *     Parse and flatten an arbitrary SQL clause with escaped parameters.
     * </p>
     * <p>
     *     To include a arbitrary length collection of parameters, such as <code>IN (?, ?, ?)</code>,
     *     use <code>"IN (?)"</code> where the parameter varargs at the corresponding index is a {@link java.util.Collection}.
     *     Then, the single <code>?</code> will be replaced/interpolated by the correct length of parameters.
     * </p>
     * <p>For example,</p>
     * <pre>flatten("id IN (?) OR flag = ?", List.of(1, 2, 3), hasFlag)</pre>
     * <p>will be converted into</p>
     * <pre>"id IN (?, ?, ?) OR flag = ?"</pre>
     * <p>and the parameters will be flattened into <code>1, 2, 3, hasFlag</code>.</p>
     * <p>
     *    Collections must correspond to exactly 1 parameter <code>?</code> mark.
     *    If there are more parameters (<code>?</code>) in the SQL template than varargs, an exception will be thrown.
     *    Too few varargs will throw an {@link IndexOutOfBoundsException}, and8
     *    too many will throw an {@link IllegalArgumentException}.
     * </p>
     *
     * @param sql SQL template with one parameter mark (?) per collection or normal param
     * @param params varargs parameters that may contain expandable collections
     * @throws IndexOutOfBoundsException if there are not enough varargs for the parameters.
     * @throws IllegalArgumentException if there are more varargs than parameters.
     * @return {@link FlattenedParameters} parsed data of potentially expanded parameter collections.
     */
    public static FlattenedParameters of(String sql, Object... params) {
        List<Object> paramList = List.of(params);
        int paramIndex = 0;
        List<Object> flattenedParams = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder();
        int lastStart = 0;
        final int len = sql.length();
        for (int i = 0; i < len; ++i) {
            if (sql.charAt(i) == '?') {
                Object currentParam = paramList.get(paramIndex++);
                if (currentParam instanceof Collection<?> collection) {
                    sqlBuilder.append(sql, lastStart, i);
                    sqlBuilder.append(InternalSqlUtils.getParameters(collection.size()));
                    lastStart = i + 1;
                    flattenedParams.addAll(collection);
                } else {
                    flattenedParams.add(currentParam);
                }
            }
        }
        if (paramIndex != params.length) {
            throw new IllegalArgumentException("Found " + paramIndex + " parameters but received " + params.length);
        }
        sqlBuilder.append(sql, lastStart, len);
        return new FlattenedParameters(sqlBuilder.toString(), flattenedParams);
    }
}
