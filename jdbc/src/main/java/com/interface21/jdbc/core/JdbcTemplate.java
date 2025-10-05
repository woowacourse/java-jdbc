package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int executeUpdate(final String sql, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setStatementParameters(pstmt, parameters);
            log.debug("update : {}, params: {}", sql, Arrays.toString(parameters));
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("executeUpdate failed. sql={}, params={}", sql, Arrays.toString(parameters), e);
            throw new RuntimeException("쿼리 실행 실패", e);
        }
    }

    private void setStatementParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }

    public <T> T executeQueryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setStatementParameters(pstmt, parameters);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                T result = rowMapper.mapForObject(rs);

                if (rs.next()) {
                    throw new IllegalStateException("결과 2건 이상");
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("쿼리 실행 실패", e);
        }
    }

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        List<T> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setStatementParameters(pstmt, parameters);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.mapForObject(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("쿼리 실행 실패", e);
        }
        return results;
    }
}
