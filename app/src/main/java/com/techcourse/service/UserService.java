package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplateException;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return doService(connection -> userDao.findById(connection, id));
    }

    private <T> T doService(final TransactionalDaoExecutor<T> executor) {
        try {
            Connection connection = dataSource.getConnection();
            return doDaoWithTransaction(executor, connection);
        } catch (SQLException e) {
            throw new DataBaseAccessException(e.getMessage());
        }
    }

    private static <T> T doDaoWithTransaction(final TransactionalDaoExecutor<T> executor,
                                              final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try {
            return executor.execute(connection);
        } catch (JdbcTemplateException e) {
            connection.rollback();
            throw new DataBaseAccessException(e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void insert(final User user) {
        doService(connection -> {
            userDao.insert(connection, user);
            return null;
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        doService(connection -> {
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            return null;
        });
    }
}
