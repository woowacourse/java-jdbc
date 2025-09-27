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

    private <T> T execute(String sql, StatementExecutor<T> executor, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            log.debug("query : {}", sql);

            return executor.execute(pstmt);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // INSERT, UPDATE, DELETE
    //TODO: 별도의 콜백 인터페이스 구현하면 Objcet...처럼 순서에 의존하지 않아도 된다고 함. 다음 스텝에..  (2025-09-27, 토, 17:38)
    public int update(String sql, Object... params) {
        return execute(sql, PreparedStatement::executeUpdate, params);
    }

    // SELECT
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        }, params);
    }

    // SELECT 단일
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        return results.stream()
                .findFirst()
                .orElse(null);
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
