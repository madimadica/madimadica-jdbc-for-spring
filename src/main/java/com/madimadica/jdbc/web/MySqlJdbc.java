package com.madimadica.jdbc.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

/**
 * Wrapper for Spring JDBC with MySQL dialect.
 */
@Component
public class MySqlJdbc implements JdbcWithImplicitBatchInsertID {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Construct this as a bean
     * @param jdbc autowired JdbcTemplate instance
     * @param namedJdbc autowired NamedParameterJdbcTemplate instance
     */
    public MySqlJdbc(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
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

    /**
     * <p>
     *     Wrap an identifier into a standard quoted MySQL identifier.
     * </p>
     * <p>
     *     For example, <code>foo.bar.baz => `foo`.`bar`.`baz`</code> or <code>foo.`bar` => `foo`.`bar`</code>
     * </p>
     * <p>
     *     Replaces '`' and '`', splits on '.', and joins back together with '`' and '`' around each identifier part.
     * </p>
     * <p>
     *     Note that literal <code>`</code> in identifiers are not supported at this time. so table `foo``bar` would be broken.
     * </p>
     * @param identifier String identifier to a properly quoted identifier
     * @return quoted identifier
     */
    @Override
    public String wrapIdentifier(String identifier) {
        identifier = identifier.trim();
        // Replace all '`', '`'
        StringBuilder sb = new StringBuilder();
        int lastStart = 0;
        int len = identifier.length();
        for (int i = 0; i < len; ++i) {
            char ch = identifier.charAt(i);
            if (ch == '`') {
                sb.append(identifier, lastStart, i);
                lastStart = i + 1;
            }
        }
        sb.append(identifier, lastStart, len);
        // Split by '.' and join back with escapes
        List<String> parts = InternalUtils.splitChar(sb.toString(), '.');
        StringJoiner fullIdentifier = new StringJoiner(".");
        for (String part : parts) {
            fullIdentifier.add('`' + part + '`');
        }
        return fullIdentifier.toString();
    }

}
