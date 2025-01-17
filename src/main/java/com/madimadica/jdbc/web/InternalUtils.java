package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.BatchInsert;
import com.madimadica.jdbc.api.RowInsert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Internal utils used across different Jdbc implementations
 */
class InternalUtils {

    /**
     * Split a string into a list of strings by a single character
     * <p>
     *     This is like calling <code>s.split(c, -1)</code>,
     *     so consecutive matches and starting/ending matches all get split
     * </p>
     * @param s string to split
     * @param c char to split by
     * @return list of split parts
     */
    static List<String> splitChar(String s, char c) {
        List<String> parts = new ArrayList<>();
        int lastStart = 0;
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (s.charAt(i) == c) {
                parts.add(s.substring(lastStart, i));
                lastStart = i + 1;
            }
        }
        parts.add(s.substring(lastStart, len));
        return parts;
    }

    /**
     * Copied from com.madimadica:madimadica-utils Lists#partitionBySize
     *
     * @param list List to partition
     * @param partitionSize Maximum size of each resulting partition
     * @return An immutable list of immutable partitions of size up to and including {@code partitionSize}
     * @param <T> Type of list elements
     */
    static <T> List<List<T>> partitionBySize(List<T> list, int partitionSize) {
        final int originalSize = list.size();
        if (originalSize < partitionSize) {
            // Single partition
            return List.of(Collections.unmodifiableList(list));
        }
        final int totalPartitions = (int) Math.ceil(originalSize / (double) partitionSize);
        List<List<T>> partitions = new ArrayList<>(totalPartitions);
        List<T> currentPartition = new ArrayList<>();
        for (T t : list) {
            currentPartition.add(t);
            if (currentPartition.size() == partitionSize) {
                // Partition is full, start a new one
                partitions.add(Collections.unmodifiableList(currentPartition));
                currentPartition = new ArrayList<>();
            }
        }
        if (!currentPartition.isEmpty()) {
            partitions.add(Collections.unmodifiableList(currentPartition));
        }
        return Collections.unmodifiableList(partitions);
    }

    /**
     * Helper method to generate an INSERT INTO statement from a {@link BatchInsert} object
     * @param jdbcImpl implementation used by this database
     * @param batchInsert batch insert data to generate off of
     * @return SQL query string, with escaped parameters, if any
     */
    static <T> String generateInsertSql(MadimadicaJdbc jdbcImpl, BatchInsert<T> batchInsert) {
        StringJoiner columnNames = new StringJoiner(", ", " (", ")");
        for (String col : batchInsert.escapedMappings().keySet()) {
            columnNames.add(jdbcImpl.wrapIdentifier(col));
        }
        for (String col : batchInsert.escapedConstants().keySet()) {
            columnNames.add(jdbcImpl.wrapIdentifier(col));
        }
        for (String col : batchInsert.unescapedConstants().keySet()) {
            columnNames.add(jdbcImpl.wrapIdentifier(col));
        }

        StringJoiner values = new StringJoiner(", ", "(", ")");
        for (int i = 0, LUB = batchInsert.escapedMappings().size() + batchInsert.escapedConstants().size(); i < LUB; ++i) {
            values.add("?");
        }
        for (Object value : batchInsert.unescapedConstants().values()) {
            values.add(String.valueOf(value));
        }

        return "INSERT INTO " +
                jdbcImpl.wrapIdentifier(batchInsert.tableName()) +
                columnNames +
                " VALUES " +
                values;
    }

    /**
     * Helper method to generate an INSERT INTO statement from a {@link RowInsert} object
     * @param jdbcImpl implementation used by this database
     * @param rowInsert insert data to generate off of
     * @return SQL query string, with escaped parameters, if any
     */
    static String generateInsertSql(MadimadicaJdbc jdbcImpl, RowInsert rowInsert) {
        StringJoiner columnNames = new StringJoiner(", ", " (", ")");
        for (String col : rowInsert.escapedValues().keySet()) {
            columnNames.add(jdbcImpl.wrapIdentifier(col));
        }
        for (String col : rowInsert.unescapedValues().keySet()) {
            columnNames.add(jdbcImpl.wrapIdentifier(col));
        }

        StringJoiner values = new StringJoiner(", ", "(", ")");
        for (int i = 0, LUB = rowInsert.escapedValues().size(); i < LUB; ++i) {
            values.add("?");
        }
        for (Object value : rowInsert.unescapedValues().values()) {
            values.add(String.valueOf(value));
        }

        return "INSERT INTO " +
                jdbcImpl.wrapIdentifier(rowInsert.tableName()) +
                columnNames +
                " VALUES " +
                values;
    }
}
