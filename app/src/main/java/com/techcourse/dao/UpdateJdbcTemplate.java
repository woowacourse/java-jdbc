package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UpdateJdbcTemplate {

    private DataSource dataSource;

    public UpdateJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(User user) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            final String sql = "update users set password=? where id =?";
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            setValuesForUpdate(user, pstmt);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getPassword());
        pstmt.setLong(2, user.getId());
        pstmt.executeUpdate();
    }
}
