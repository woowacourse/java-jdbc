package com.techcourse.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public Object query(String query, RowMapper rowMapper) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            setValues(pstmt);
            return mapUser(pstmt.executeQuery(), rowMapper);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Object mapUser(ResultSet resultSet, RowMapper rowMapper) throws SQLException;

    public abstract void setValues(PreparedStatement pstmt) throws SQLException;
}
