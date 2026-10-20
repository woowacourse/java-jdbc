package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateJdbcTemplate {

    private final DataSource dataSource;

    public UpdateJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final User user, final UserDao userDao) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(userDao.createQueryForUpdate())) {
            userDao.setValuesForUpdate(user, pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
