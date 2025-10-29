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

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao,
                       final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        return userDao.findById(conn, id);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                log.error("rollback");
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
