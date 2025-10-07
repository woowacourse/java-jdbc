package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void update(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        }, params);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        log.debug("query : {}", sql);
                        List<T> results = new ArrayList<>();
                        while (rs.next()) {
                            results.add(rowMapper.mapRow(rs));
                        }
                        return results;
                    }
                }
                , params);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> actions, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setPreparedStatement(pstmt, params);
            return actions.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setPreparedStatement(PreparedStatement pstmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
