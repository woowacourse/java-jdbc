package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(DataSource dataSource, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            changePasswordInTransaction(connection, id, newPassword, createBy);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void changePasswordInTransaction(Connection connection, long id, String newPassword, String createBy)
            throws SQLException {
        try {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            UserHistory userHistory = new UserHistory(user, createBy);
            userHistoryDao.log(connection, userHistory);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
