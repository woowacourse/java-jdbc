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
        ResultSet rs = null;
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            rs = pstmt.executeQuery();
            return rowMapper.mapForObject(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("리소스 해제에 실패했습니다.");
        } finally {
            closeResultSet(rs);
        }
    }

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        ResultSet rs = null;
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            rs = pstmt.executeQuery();
            return rowMapper.mapForObjects(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("리소스 해제에 실패했습니다.");
        } finally {
            closeResultSet(rs);
        }
    }

    private void setStatementParameters(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            final Object parameter = parameters[i];
            if (parameter instanceof String) {
                pstmt.setString(i + 1, (String) parameter);
            }
            if (parameter instanceof Long) {
                pstmt.setLong(i + 1, (Long) parameter);
            }
        }
    }

    private void closeResultSet(final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
