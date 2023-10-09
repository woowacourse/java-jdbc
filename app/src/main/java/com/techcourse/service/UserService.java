package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.core.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    public User findById(final long id) throws SQLException {
        return transactionTemplate.transaction(() -> userDao.findById(id));
    }

    public void insert(final User user) throws SQLException {
        transactionTemplate.transaction(() -> {
            userDao.insert(user);
            return user;
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        transactionTemplate.transaction(() -> {
            User user = userDao.findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            return user;
        });
    }
}
