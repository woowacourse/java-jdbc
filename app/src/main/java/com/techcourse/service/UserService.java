package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public Optional<User> findById(final long id) {
        try (Connection connection = dataSource.getConnection()) {
            return userDao.findById(connection, id);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Optional<User> findByAccount(String account) {
        try (Connection connection = dataSource.getConnection()) {
            return userDao.findByAccount(connection, account);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void insert(final User user) {
        try (Connection connection = dataSource.getConnection()) {
            userDao.insert(connection, user);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            User user = findById(id).orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new DataAccessException(e);
        } finally {
            connection.close();
        }
    }
}
