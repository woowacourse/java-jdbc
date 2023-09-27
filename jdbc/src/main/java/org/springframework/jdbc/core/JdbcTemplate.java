package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = setPreparedStatement(conn, sql, args);
             final ResultSet rs = pstmt.executeQuery()
        ) {
            conn.setReadOnly(true);
            conn.setAutoCommit(false);
            if (rs.last()) {
                validateSingleRow(rs);
                return rowMapper.mapRow(rs);
            }
            conn.commit();
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement setPreparedStatement(
            final Connection connection,
            final String sql,
            final Object... args
    ) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        bindArguments(preparedStatement, args);
        return preparedStatement;
    }

    private void bindArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int index = 0; index < args.length; index++) {
            pstmt.setObject(index + 1, args[index]);
        }
    }

    private void validateSingleRow(final ResultSet rs) throws SQLException {
        if (rs.getRow() != 1) {
            throw new IllegalArgumentException("조회 결과가 2개 이상입니다.");
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = setPreparedStatement(conn, sql, args);
             final ResultSet rs = pstmt.executeQuery()
        ) {
            conn.setReadOnly(true);
            conn.setAutoCommit(false);
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            conn.commit();
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void execute(final String sql, final Object... args) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            final PreparedStatement pstmt = setPreparedStatement(conn, sql, args);
            conn.setAutoCommit(false);
            pstmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

}
