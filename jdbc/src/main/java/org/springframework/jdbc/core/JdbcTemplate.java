package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void setParameters(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, args);
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, args);
            try (final ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
