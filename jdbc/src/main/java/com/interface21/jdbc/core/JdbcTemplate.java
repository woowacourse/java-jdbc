package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T select(final String sql, final RowMapper<T> rowMapper, final Object... values) {
        return execute(sql, new QueryObjectCallback<>(rowMapper), values);
    }

    public <T> List<T> selectList(final String sql, final RowMapper<T> rowMapper, final Object... values) {
        return execute(sql, new QueryListCallback<>(rowMapper), values);
    }

    public void update(final String sql, final Object... values) {
        execute(sql, new UpdateCallback(), values);
    }

    public <T> T execute(final String sql, final JdbcCallback<T> callback, final PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(pstmt);
            return callback.call(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final String sql, final JdbcCallback<T> callback, final Object... values) {
        return execute(sql, callback, createPreparedStatementSetter(values));
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... values) {
        return pstmt -> {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
        };
    }
}
