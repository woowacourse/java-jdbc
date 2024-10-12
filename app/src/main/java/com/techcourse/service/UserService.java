package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceConfig.getTxConnection();
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            connection.commit();
        } catch (final Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (final SQLException exception) {
                    throw new DataAccessException("롤백 과정에서 예외가 발생했습니다.");
                }
            }
            throw new DataAccessException("Data source 커넥션 획득에 실패했습니다.");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (final SQLException e) {
                }
            }
        }
    }
}
