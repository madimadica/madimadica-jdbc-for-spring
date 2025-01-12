package com.madimadica.jdbc.api;

import java.util.StringJoiner;

/**
 * Package private, internal utilities for building SQL strings
 */
class InternalSqlUtils {

    /**
     * Prevent constructing instances
     */
    private InternalSqlUtils() {}

    /**
     * Convert a size into a CSV of parameters. For example,
     * <pre>
     *     getParameters(0) // ""
     *     getParameters(1) // "?"
     *     getParameters(2) // "?, ?"
     *     getParameters(3) // "?, ?, ?"
     * </pre>
     * @param size number of parameters to create
     * @return parameterized string
     */
    static String getParameters(int size) {
        StringJoiner sj = new StringJoiner(", ");
        for (int i = 0; i < size; ++i) {
            sj.add("?");
        }
        return sj.toString();
    }


}
