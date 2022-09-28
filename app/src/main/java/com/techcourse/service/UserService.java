package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final JdbcTemplate jdbcTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final JdbcTemplate jdbcTemplate1) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.jdbcTemplate = jdbcTemplate1;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void edit(final User user, final String createBy) {
        final var connection = jdbcTemplate.getConnection();

        try (connection) {
            connection.setAutoCommit(false);

            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        }
    }
}
