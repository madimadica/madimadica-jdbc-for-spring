package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.BatchUpdate;
import com.madimadica.jdbc.api.RowUpdate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;
import java.util.function.Function;

/**
 * API Specification for a Jdbc wrapper class.
 */
public interface MadimadicaJdbc {

    /**
     * Get the underlying JdbcTemplate instance
     * @return {@link JdbcTemplate} bean
     */
    JdbcTemplate getJdbcTemplate();

    /**
     * Get the underlying NamedParameterJdbcTemplate instance
     * @return {@link NamedParameterJdbcTemplate} bean
     */
    NamedParameterJdbcTemplate getNamedJdbcTemplate();

    /**
     * <p>
     *     Wrap an identifier into a standard quoted identifier, as per the dialect.
     * </p>
     * <p>
     *     Normalizes quoting across identifier parts.
     * </p>
     * @param identifier String identifier to a properly quoted identifier
     * @return quoted identifier
     */
    String wrapIdentifier(String identifier);

    /**
     * <p>
     *     Perform a row update query based on the given parameter object.
     * </p>
     * <p>
     *     This is not wrapped in a transaction. The user must do that themselves, if desired.
     * </p>
     * @see RowUpdate
     * @param rowUpdate {@link RowUpdate} parameter object
     */
    default void update(RowUpdate rowUpdate) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(wrapIdentifier(rowUpdate.tableName()));
        sql.append(" SET ");

        StringJoiner setters = new StringJoiner(", ");
        for (var col : rowUpdate.escapedUpdates().keySet()) {
            setters.add(wrapIdentifier(col) + " = ?");
        }
        for (var entry : rowUpdate.unescapedUpdates().entrySet()) {
            setters.add(wrapIdentifier(entry.getKey()) + " = " + entry.getValue());
        }
        sql.append(setters);
        sql.append(" WHERE ");
        sql.append(rowUpdate.whereClause());

        Object[] params = rowUpdate.getParams().toArray();
        getJdbcTemplate().update(sql.toString(), params);
    }

    /**
     * <p>
     *     Perform a batch of update queries based on the given parameter object.
     * </p>
     * <p>
     *     This is not wrapped in a transaction. The user must do that themselves, if desired.
     * </p>
     * @see BatchUpdate
     * @param batchUpdate {@link BatchUpdate} parameter object
     * @param <T> type of row elements
     */
    default <T> void update(BatchUpdate<T> batchUpdate) {
        if (batchUpdate.isEmpty()) {
            return;
        }
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(wrapIdentifier(batchUpdate.tableName()));
        sql.append(" SET ");

        StringJoiner setters = new StringJoiner(", ");
        for (var col : batchUpdate.escapedMappings().keySet()) {
            setters.add(wrapIdentifier(col) + " = ?");
        }
        for (var col : batchUpdate.escapedConstants().keySet()) {
            setters.add(wrapIdentifier(col) + " = ?");
        }
        for (var entry : batchUpdate.unescapedConstants().entrySet()) {
            setters.add(wrapIdentifier(entry.getKey()) + " = " + entry.getValue());
        }
        sql.append(setters);
        sql.append(" WHERE ");
        sql.append(batchUpdate.whereClause());

        final Collection<Function<? super T, Object>> escapedMappers = batchUpdate.escapedMappings().values();
        final Collection<Object> escapedConstants = batchUpdate.escapedConstants().values();
        final int paramCount = batchUpdate.getParamCount();
        final List<Object[]> allParams = new ArrayList<>(batchUpdate.rows().size());

        for (T row : batchUpdate.rows()) {
            Object[] args = new Object[paramCount];
            int i = 0;
            for (var mapper : escapedMappers) {
                args[i++] = mapper.apply(row);
            }
            for (var constant : escapedConstants) {
                args[i++] = constant;
            }
            for (var mapper : batchUpdate.whereParamMappers()) {
                args[i++] = mapper.apply(row);
            }
            allParams.add(args);
        }
        getJdbcTemplate().batchUpdate(sql.toString(), allParams);
    }

}
