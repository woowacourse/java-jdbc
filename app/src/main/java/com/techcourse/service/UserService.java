package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            final var user = userDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Rollback failed", ex);
                }
            }
            throw new DataAccessException(e);
        } finally {
            if (connection != null) {
                try {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                    Connection unboundResource = TransactionSynchronizationManager.unbindResource(dataSource);
                    log.info("Resource has been unbound: {}", unboundResource);
                } catch (Exception ex) {
                    log.error("Failed to release connection or unbind resource", ex);
                }
            }
        }
    }
}
