package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlExecutionException("SQL 실행 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public void update(final String sql, final Object... params) {
        update(sql, pstmt -> {
            try {
                setParameters(pstmt, params);
            } catch (SQLException e) {
                throw new SqlExecutionException("파라미터 설정 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        });
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter pss, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pss.setValues(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractResults(rs, rowMapper);
            }
        } catch (SQLException e) {
            throw new SqlExecutionException("SQL 실행 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RowMappingException("ResultSet을 객체로 매핑하는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return query(sql, pstmt -> {
            try {
                setParameters(pstmt, params);
            } catch (SQLException e) {
                throw new SqlExecutionException("파라미터 설정 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }, rowMapper);
    }

    private <T> List<T> extractResults(ResultSet rs, RowMapper<T> rowMapper) {
        try {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new RowMappingException("ResultSet 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        List<T> results = query(sql, rowMapper, params);

        if (results.isEmpty()) {
            throw new InvalidResultException("조회 결과가 없습니다");
        }
        if (results.size() > 1) {
            throw new InvalidResultException("조회 결과가 2개 이상입니다");
        }

        return results.getFirst();
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
