package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.RowUpdate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Wrapper for Spring JDBC with SQL Server dialect.
 */
@Component
public class SqlServerJdbc {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Construct this as a bean
     * @param jdbc autowired JdbcTemplate instance
     * @param namedJdbc autowired NamedParameterJdbcTemplate instance
     */
    public SqlServerJdbc(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

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
    public void update(RowUpdate rowUpdate) {
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

        List<Object> params = new ArrayList<>(rowUpdate.escapedUpdates().values());
        params.addAll(rowUpdate.unescapedUpdates().values());

        this.jdbc.update(sql.toString(), params.toArray());
    }

    /**
     * <p>
     *     Wrap an identifier into a standard quoted SQL Server identifier.
     * </p>
     * <p>
     *     For example, <code>foo.bar.baz => [foo].[bar].[baz]</code> or <code>foo.[bar] => [foo].[bar]</code>
     * </p>
     * <p>
     *     Replaces '[' and ']', splits on '.', and joins back together with '[' and ']' around each identifier part.
     * </p>
     *
     * @param identifier String identifier to a properly quoted identifier
     * @return quoted identifier
     */
    private static String wrapIdentifier(String identifier) {
        identifier = identifier.trim();
        // Replace all '[', ']'
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        int len = identifier.length();
        for (int i = 0; i < len; ++i) {
            char ch = identifier.charAt(i);
            if (ch == '[' || ch == ']') {
                sb.append(identifier, lastStart, i);
                lastStart = i + 1;
            }
        }
        sb.append(identifier, lastStart, len);
        // Split by '.' and join back with escapes
        List<String> parts = InternalStringUtils.splitChar(sb.toString(), '.');
        StringJoiner fullIdentifier = new StringJoiner(".");
        for (String part : parts) {
            fullIdentifier.add('[' + part + ']');
        }
        return fullIdentifier.toString();
    }

}
