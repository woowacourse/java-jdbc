package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;
import javax.sql.DataSource;
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
        return userDao.findById(id)
                .orElseThrow();
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        DataSource dataSource = userDao.getDataSource();
        executeInTransaction(dataSource, conn -> {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
            return null;
        });
    }

    private <T> T executeInTransaction(DataSource dataSource, Function<Connection, T> callback) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            return execute(callback, conn);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Function<Connection, T> callback, Connection conn) throws SQLException {
        try {
            T result = callback.apply(conn);
            conn.commit();
            return result;
        } catch (SQLException | DataAccessException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new DataAccessException(e);
        }
    }
}
