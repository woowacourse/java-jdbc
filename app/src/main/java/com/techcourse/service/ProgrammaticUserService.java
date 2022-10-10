package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class ProgrammaticUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSourceTransactionManager manager = new DataSourceTransactionManager(
            DataSourceConfig.getInstance());

    public ProgrammaticUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id);
    }

    @Override
    public void insert(final User user) {
        userDao.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus status = manager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            manager.commit(status);
        } catch (RuntimeException e) {
            manager.rollback(status);
            throw e;
        }
    }
}
