package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.h2.util.JdbcUtils;

public class UserService {

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

    public void changePassword(final long id, final String newPassword, final String createBy) {
        processInTransaction(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user, connection);
            userHistoryDao.log(new UserHistory(user, createBy), connection);
        });
    }

    private void processInTransaction(final Consumer<Connection> consumer) {
        Connection connection = null;
        try  {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } catch (RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            JdbcUtils.closeSilently(connection);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {}
    }
}
