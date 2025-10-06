package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public void update(final String sql, final Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(final String sql, RowMapper<T> mapper, final Object... params) {
        return execute(sql, ps ->{
            try (ResultSet resultSet = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(mapper.mapRow(resultSet));
                }

                return result;
            }
        }, params);
    }

    public <T> T queryForObject(final String sql, RowMapper<T> mapper, final Object... params) {
        return execute(sql, ps ->{
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    T result = mapper.mapRow(resultSet);
                    if (resultSet.next()) {
                        throw new IllegalStateException("Expected single row");
                    }
                    return result;
                }

                return null;
            }
        }, params);
    }

    private <R> R execute(String sql, PreparedStatementCallBack<R> action, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            log.debug("query : {}", sql);

            bindParams(pstmt, params);

            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void bindParams(final PreparedStatement pstmt, final Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        int index = 1;
        for (Object value : params) {
            pstmt.setObject(index++, value);
        }
    }
}
