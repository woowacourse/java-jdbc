package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.TransactionExecutor;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class UserService {

    private final TransactionExecutor transactionExecutor;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(DataSource dataSource, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.transactionExecutor = new TransactionExecutor(dataSource);
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        transactionExecutor.execute(connection -> {
            userDao.insert(connection, user);
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionExecutor.execute(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
