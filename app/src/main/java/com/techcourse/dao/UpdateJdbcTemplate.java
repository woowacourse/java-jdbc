package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(UpdateJdbcTemplate.class);

    public void execute(User user, UserDao userDao) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = userDao.getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);

            setValueForUpdate(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void setValueForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }
}
