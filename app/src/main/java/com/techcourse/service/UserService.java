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
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService() {
        this(
                DataSourceConfig.getInstance(),
                new UserDao(DataSourceConfig.getInstance()),
                new UserHistoryDao(DataSourceConfig.getInstance())
        );
    }

    public UserService(DataSource dataSource, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;

        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (SQLException e) {
            rollbackSafely(connection);
            throw new DataAccessException("트랜잭션 처리 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            rollbackSafely(connection);
            throw new DataAccessException(e);
        } finally {
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollbackSafely(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
    }

}

