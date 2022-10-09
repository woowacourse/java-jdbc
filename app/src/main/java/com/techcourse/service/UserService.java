package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;

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
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id로 사용자를 찾을 수 없습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);

            User user = findById(id);
            user.changePassword(newPassword);
            userDao.updateForTransaction(user, connection);
            userHistoryDao.log(new UserHistory(user, createBy), connection);

            connection.commit();

        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);

        } finally {
            close(connection);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void close(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
