package com.techcourse.service;

import com.interface21.transaction.support.TransactionCallback;
import com.interface21.transaction.support.TransactionFactory;
import com.interface21.transaction.support.TransactionalTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionalTemplate transactionalTemplate;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionalTemplate = new TransactionalTemplate();
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final TransactionCallback callback = (transaction -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(transaction.getConnection(), user);
            userHistoryDao.log(transaction.getConnection(), new UserHistory(user, createBy));
        });

        transactionalTemplate.execute(TransactionFactory.create(DataSourceConfig.getInstance()), callback);
    }


}
