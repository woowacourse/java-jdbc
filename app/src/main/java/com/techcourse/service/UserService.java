package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            final User findUser = userDao.findById(connection, id);

            connection.commit();

            return findUser;
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("jdbc 연결에 실패했습니다.");
        }
    }

    public void insert(final User user) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            userDao.insert(connection, user);

            connection.commit();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("jdbc 연결에 실패했습니다.");
        }
    }

    public void changePassword(long id, final String newPassword, final String createBy) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("jdbc 연결에 실패했습니다.");
        }
    }
}
