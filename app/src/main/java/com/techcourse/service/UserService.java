package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.JdbcExecutionException;
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

    // Transaction 적용하기
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        Connection connection = null;
        try {
            connection = userDao.getJdbcTemplate()
                    .getDataSource()
                    .getConnection();
            // transaction start
            connection.setAutoCommit(false);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (DataAccessException e) {
            safeRollback(connection, e);
            throw e;
        } catch (SQLException sqlException){
            throw new RuntimeException("롤백 실패");
        }
        finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void safeRollback(Connection conn, Exception cause) {
        if(conn == null){
            log.debug("Connection is null");
            return;
        }
        try { conn.rollback(); } catch (SQLException rb) { cause.addSuppressed(rb); }
    }
}
