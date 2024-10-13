package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

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

    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try (connection) {
            connection.setAutoCommit(false);

            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);

            UserHistory userHistory = new UserHistory(user, createBy);
            userHistoryDao.log(userHistory);

            connection.commit();
        } catch (Exception e) {
            rollbackTransaction(connection);
            log.info("CHANGE_PASSWORD_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("비밀번호를 변경하던 중 예외가 발생했습니다.");
        } finally {
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }

    private void rollbackTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.info("ROLLBACK_TRANSACTION_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("트랜잭션을 롤백하던 중 예외가 발생했습니다.");
        }
    }
}
