package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final DataSource dataSource;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, DataSource dataSource, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.dataSource = dataSource;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        try (Connection connection = dataSource.getConnection()){
            return userDao.findById(id, connection);
        } catch (SQLException e) {
            throw new InvalidRequestException();
        }
    }

    public void insert(final User user) {
        try (Connection connection = dataSource.getConnection()){
            userDao.insert(user, connection);
        } catch (SQLException e) {
            throw new InvalidRequestException();
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = dataSource.getConnection()){
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user, connection);
            userHistoryDao.log(new UserHistory(user, createBy));
        } catch (SQLException e) {
            throw new InvalidRequestException();
        }
    }
}
