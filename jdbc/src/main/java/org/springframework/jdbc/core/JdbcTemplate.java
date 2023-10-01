package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return executeQuery(rowMapper, pstmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static <T> Optional<T> executeQuery(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        try (final ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return Optional.ofNullable(rowMapper.mapRow(rs));
            }
            return Optional.empty();
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            final List<T> result = new ArrayList<>();
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }

        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public int update(final String sql, final PreparedStatementSetter pstmtSetter) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmtSetter.setValues(pstmt);
            return pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
