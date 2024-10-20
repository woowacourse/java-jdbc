package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            preparedStatementSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper,
                                final PreparedStatementSetter preparedStatementSetter) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            preparedStatementSetter.setValues(pstmt);
            List<T> result = mapResultSetToList(rowMapper, pstmt);
            if (result.size() != 1) {
                throw new DataAccessException();
            }
            return result.getFirst();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> mapResultSetToList(final RowMapper<T> rowMapper, final PreparedStatement pstmt)
            throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> queryResult = new ArrayList<>();
            while (rs.next()) {
                queryResult.add(rowMapper.mapRow(rs));
            }
            return queryResult;
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return mapResultSetToList(rowMapper, pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
