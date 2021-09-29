package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class JdbcTemplate {

    protected abstract DataSource getDatasource();

    protected abstract String createQuery();

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    public void update() throws SQLException {
        final String sql = createQuery();

        Connection conn = getDatasource().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        try (conn; pstmt) {

            setValues(pstmt);
            pstmt.executeUpdate();
        }
    }

}
