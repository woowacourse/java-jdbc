package com.techcourse.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class InsertJdbcTemplate {

    private final DataSource dataSource;

    public InsertJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert() {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValuesForInsert(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update() {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValuesForInsert(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String createQuery();

    public abstract void setValuesForInsert(PreparedStatement pstmt) throws SQLException;
}
