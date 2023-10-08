package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return startTransaction(connection -> userDao.findById(connection, id));
    }

    public void insert(final User user) {
        startTransaction(connection -> userDao.insert(connection, user));
    }

    public void changePassword(long id, final String newPassword, final String createBy) {
        startTransaction(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            return userDao.update(connection, user);
        });
    }

    public <T> T startTransaction(final TransactionExecutor<T> transactionExecutor) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            return commitTransaction(transactionExecutor, connection);
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("jdbc 연결에 실패했습니다.");
        }
    }

    private <T> T commitTransaction(final TransactionExecutor<T> transactionExecutor, final Connection connection) throws SQLException {
        try {
            final T result = transactionExecutor.execute(connection);

            connection.commit();

            return result;
        } catch (Exception ex) {
            connection.rollback();

            log.error(ex.getMessage());
            throw new DataAccessException("실행 중 예외가 발생했습니다.");
        }
    }
}
