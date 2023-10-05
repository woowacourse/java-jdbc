package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);

                User user = userDao.findById(con, id);

                con.commit();
                return user;
            } catch (Exception e) {
                con.rollback();
                throw new DataAccessException(e);
            } finally {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void insert(final User user) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);

                userDao.insert(con, user);

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw new DataAccessException(e);
            } finally {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection con = dataSource.getConnection()) {
            try {
                con.setAutoCommit(false);

                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(con, user);
                userHistoryDao.log(con, new UserHistory(user, createBy));

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw new DataAccessException(e);
            } finally {
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
