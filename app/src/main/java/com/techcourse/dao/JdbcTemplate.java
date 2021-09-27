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
            setValuesForInsert(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Object query(String query) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            setValuesForInsert(pstmt);
            return mapUser(pstmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Object mapUser(ResultSet resultSet) throws SQLException;

    public abstract void setValuesForInsert(PreparedStatement pstmt) throws SQLException;
}
