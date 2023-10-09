package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionTemplate transactionTemplate) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = transactionTemplate;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        transactionTemplate.execute(connection -> userDao.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionTemplate.execute(connection -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
        });
    }
}
