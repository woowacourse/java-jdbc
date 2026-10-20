package com.techcourse.dao;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractJdbcTemplate {

    private final DataSource dataSource;

    protected AbstractJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValues(pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    protected abstract String createQuery();

    protected abstract void setValues(final PreparedStatement pstmt) throws SQLException;
}
