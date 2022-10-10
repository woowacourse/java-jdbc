package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.support.TransactionService;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService extends TransactionService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        super(DataSourceConfig.getInstance(), new DefaultTransactionDefinition());
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        runWithTransaction(() -> userDao.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        runWithTransaction(() -> {
            final User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
