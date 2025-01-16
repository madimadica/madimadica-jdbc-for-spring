package com.madimadica.jdbc.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

/**
 * Wrapper for Spring JDBC with SQL Server dialect.
 */
@Component
public class SqlServerJdbc implements JdbcWithExplicitBatchInsertID {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlServerJdbc.class);
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Parameter limit for SQL Server driver
     */
    public static final int MAX_PARAMS_PER_QUERY = 2099;
    /**
     * Insert Limit for SQL Server driver
     */
    public static final int MAX_INSERTS_PER_QUERY = 1000;

    /**
     * Construct this as a bean
     * @param jdbc autowired JdbcTemplate instance
     * @param namedJdbc autowired NamedParameterJdbcTemplate instance
     */
    public SqlServerJdbc(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbc;
    }

    @Override
    public NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        return namedJdbc;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
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
    @Override
    public String wrapIdentifier(String identifier) {
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
        List<String> parts = InternalUtils.splitChar(sb.toString(), '.');
        StringJoiner fullIdentifier = new StringJoiner(".");
        for (String part : parts) {
            fullIdentifier.add('[' + part + ']');
        }
        return fullIdentifier.toString();
    }

    @Override
    public int getMaxParametersPerQuery() {
        return MAX_PARAMS_PER_QUERY;
    }

    @Override
    public int getMaxInsertsPerQuery() {
        return MAX_INSERTS_PER_QUERY;
    }

}
