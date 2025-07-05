package com.madimadica.jdbc.web;

import com.madimadica.jdbc.api.RowInsert;

/**
 * For use with JDBC implementations that can insert single rows at a time.
 * The specific variant of this to implement depends on if you can implicitly or explicitly
 * obtain generated values from the inserted row.
 */
public sealed interface JdbcRowInsertWithId extends MadimadicaJdbc permits
        JdbcRowInsertWithImplicitId,
        JdbcRowInsertWithExplicitId {

    /**
     * INSERT a single row using the given {@link RowInsert} parameter object.
     * @param rowInsert configured row insert parameter
     * @return number of rows affected.
     */
    default int insert(RowInsert rowInsert) {
        String sql = InternalUtils.generateInsertSql(this, rowInsert);
        getLogger().debug(sql);
        return getJdbcTemplate().update(sql, rowInsert.escapedValues().values().toArray());
    }

}
