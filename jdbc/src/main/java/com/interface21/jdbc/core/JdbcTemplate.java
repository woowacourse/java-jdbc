package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            return callback.doInStatement(pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T query(final String sql, final PreparedStatementSetter pstmtSetter, final RowMapper<T> rowMapper) {
        return execute(sql, (pstmt) -> {
            pstmtSetter.setValues(pstmt);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rowMapper.mapRow(rs);
            }
        });
    }

    public void update(final String sql, final PreparedStatementSetter pstmtSetter) {
        execute(sql, (pstmt) -> {
                    pstmtSetter.setValues(pstmt);
                    pstmt.executeUpdate();
                    return null;
                }
        );
    }
}
