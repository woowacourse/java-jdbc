package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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

    public <T> T queryForObject(final String sql, final RowMapper<T> mapper,
                                final Object... args) {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setPreparedStatement(pstmt, args);
                try (final ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new IncorrectResultSizeDataAccessException("조회 결과가 없습니다: " + sql);
                    }
                    final T result = mapper.mapRow(rs, 1);
                    if (rs.next()) {
                        throw new IncorrectResultSizeDataAccessException("조회 결과가 1건이 아닙니다: " + sql);
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("DB 조회에 실패했습니다. :" + sql, e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Object... args) {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            try (final PreparedStatement pstmt = conn.prepareStatement(sql);
                 final ResultSet rs = pstmt.executeQuery()) {
                setPreparedStatement(pstmt, args);

                final List<T> result = new ArrayList<>();
                int rowNum = 1;
                while (rs.next()) {
                    result.add(mapper.mapRow(rs, rowNum++));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("DB 조회에 실패했습니다. :" + sql, e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

    public void update(final String sql, final Object... args) {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setPreparedStatement(pstmt, args);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("DB 조회에 실패했습니다. :" + sql, e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

    private void setPreparedStatement(final PreparedStatement pstmt, final Object... args) {
        try {
            final int paramCount = pstmt.getParameterMetaData().getParameterCount();
            if (paramCount != args.length) {
                throw new IllegalArgumentException(
                        "SQL 파라미터 개수 불일치: expected " + paramCount + ", actual " + args.length);
            }

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("SQL 파라미터 설정 실패: " + ex.getMessage(), ex);
        }
    }
}