package com.techcourse.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class InsertJdbcTemplate {

    private final DataSource dataSource;
    private final String query;

    public InsertJdbcTemplate(DataSource dataSource, String query) {
        this.dataSource = dataSource;
        this.query = query;
    }

    public void insert() {
        try (Connection conn = createConnection();
                PreparedStatement pstmt = createPstmt(conn)) {
            setValuesForInsert(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement createPstmt(Connection conn) throws SQLException {
        return conn.prepareStatement(query);
    }

    private Connection createConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public abstract void setValuesForInsert(PreparedStatement pstmt) throws SQLException;
}
