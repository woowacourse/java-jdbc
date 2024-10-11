package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.ConnectionUtil;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (Exception e) {  // 모든 예외를 catch
            ConnectionUtil.rollback(connection);
            throw new DataAccessException(e);  // 원래 예외를 다시 던짐
        } finally {
            ConnectionUtil.setAutoCommitAndClose(connection, true);
        }
    }

    private Connection getConnection() throws SQLException {
        return DataSourceConfig.getInstance().getConnection();
    }

    public void insert(final User user) {
        try (Connection connection = getConnection()) {
            userDao.insert(connection, user);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public User findById(final long id) {
        try (Connection connection = getConnection()) {
            return userDao.findById(connection, id);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
