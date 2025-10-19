package com.techcourse.service;

import com.interface21.jdbc.exception.JdbcException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(
            final long id,
            final String newPassword,
            final String createBy
    ) {
        transaction((connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        }));
    }

    private void transaction(final Consumer<Connection> businessLogic) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            businessLogic.accept(conn);
            conn.commit();
        } catch (SQLException e) {
            connectionRollback(conn);
            throw new JdbcException(e);
        } catch (Exception e) {
            connectionRollback(conn);
            throw e;
        } finally {
            connectionClose(conn);
        }
    }

    private void connectionClose(final Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException exception) {
                throw new JdbcException(exception);
            }
        }
    }

    private void connectionRollback(final Connection conn) {
        if (conn != null) {
            try {
                log.warn("rollback");
                conn.rollback();
            } catch (SQLException exception) {
                throw new JdbcException(exception);
            }
        }
    }
}
