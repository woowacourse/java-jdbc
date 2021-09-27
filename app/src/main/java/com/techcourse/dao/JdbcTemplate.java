package com.techcourse.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public abstract class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            setValues(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper) {
        return query(query, rowMapper).get(0);
    }

    public <T> List<T> query(String query, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            setValues(pstmt);
            return doMapping(pstmt.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> doMapping(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> data = new ArrayList<>();
        while (resultSet.next()) {
            data.add(rowMapper.mapRow(resultSet));
        }

        if (data.isEmpty()) {
            throw new SQLException();
        }
        return data;
    }

    public abstract void setValues(PreparedStatement pstmt) throws SQLException;
}
