package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(Connection connection, long id) {
        return userDao.findById(connection, id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
    }

    public void insert(User user) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            userDao.insert(connection, user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changePassword(long id, String newPassword, String createBy) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            User user = findById(connection, id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
