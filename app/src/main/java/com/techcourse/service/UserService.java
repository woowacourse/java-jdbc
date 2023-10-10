package com.techcourse.service;

import com.techcourse.dao.UserDaoWithJdbcTemplate;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.SQLTransactionRollbackException;

public class UserService {

    private final UserDaoWithJdbcTemplate userDao;
    private final UserHistoryDao userHistoryDao;
    private final Connection connection;

    public UserService(final Connection connection, final UserDaoWithJdbcTemplate userDao, final UserHistoryDao userHistoryDao) {
        this.connection = connection;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        final Optional<User> user = userDao.findById(connection, id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("회원을 찾을 수 없음");
        }
        return user.get();
    }

    public void insert(final User user) {
        userDao.insert(connection, user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            connection.commit();
        } catch (RuntimeException | SQLException e) {
            handleTransactionFailure(e);
        }
    }

    private void handleTransactionFailure(final Exception e) {
        try {
            connection.rollback();
            throw new DataAccessException(e);
        } catch (SQLException ex) {
            throw new SQLTransactionRollbackException(ex);
        }
    }
}
