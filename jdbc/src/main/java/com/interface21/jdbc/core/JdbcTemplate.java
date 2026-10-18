package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(
            String sql, RowMapper<T> rowMapper, Object... params) {

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
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

    public <T> T queryForObject(
            String sql, RowMapper<T> rowMapper, Object... params) {

        List<T> results = query(sql, rowMapper, params);
        return results.isEmpty() ? null : results.get(0);
    }

    private void setParameters(
            PreparedStatement pstmt, Object... params) throws SQLException {

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
