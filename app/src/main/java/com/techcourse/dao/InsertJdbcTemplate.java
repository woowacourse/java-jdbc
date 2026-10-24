package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertJdbcTemplate {

    public InsertJdbcTemplate() {
    }

    public void insert(final User user, final UserDao userDao) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        try (final Connection conn = userDao.getDataSource().getConnection();
            final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
