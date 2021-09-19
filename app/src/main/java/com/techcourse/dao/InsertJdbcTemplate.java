package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertJdbcTemplate {

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    public void insert(User user, UserDao userDao) {
        String sql = createQueryForInsert();
        try (Connection conn = userDao.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setValuesForInsert(user, pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.executeUpdate();
    }

}
