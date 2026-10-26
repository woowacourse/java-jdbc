package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

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

    public User getById(final long id) {
        return userDao.getById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = getById(id);
        user.changePassword(newPassword);

        try (Connection connection = dataSource.getConnection()) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                connection.setAutoCommit(false);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));
                connection.commit();
            } catch (RuntimeException | SQLException | Error e) {
                connection.rollback();
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException("트랜잭션 처리 중 오류가 발생했습니다.", e);
        }
    }
}
