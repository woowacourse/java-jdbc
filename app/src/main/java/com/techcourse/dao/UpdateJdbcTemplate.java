package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateJdbcTemplate {

    private String createQueryForUpdate() {
        return "update users set password = ?, email = ? where account = ? ";
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getPassword());
        pstmt.setString(2, user.getEmail());
        pstmt.setString(3, user.getAccount());
        pstmt.executeUpdate();
    }

    public void update(User user, UserDao userDao) {
        final String sql = createQueryForUpdate();

        try (Connection conn = userDao.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setValuesForUpdate(user, pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
