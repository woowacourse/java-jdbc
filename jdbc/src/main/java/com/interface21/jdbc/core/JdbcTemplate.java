package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public int executeUpdate(final String sql, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("리소스 해제에 실패했습니다.");
        }
    }

    public <T> T executeQueryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rowMapper.mapForObject(rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("리소스 해제에 실패했습니다.");
        }
    }

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return rowMapper.mapForObjects(rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("리소스 해제에 실패했습니다.");
        }
    }

    private void setStatementParameters(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }
}
