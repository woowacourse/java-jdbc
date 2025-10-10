package com.techcourse.service;

import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionTemplate transactionTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    public User findById(final long id) {
        return (User) transactionTemplate.execute(() -> userDao.findById(id));
    }

    public void insert(final User user) {
        transactionTemplate.execute(() -> {
            userDao.insert(user);
            return null;
        });
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);

        transactionTemplate.execute(() -> {
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            return null;
        });
    }
}
