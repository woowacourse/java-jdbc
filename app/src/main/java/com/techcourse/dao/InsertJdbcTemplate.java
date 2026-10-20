package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertJdbcTemplate {

    private final DataSource dataSource;

    public InsertJdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final User user, final UserDao userDao) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(userDao.createQueryForInsert())) {
            userDao.setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
