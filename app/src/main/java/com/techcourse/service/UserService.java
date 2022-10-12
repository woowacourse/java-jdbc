package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.exception.UserNotFoundException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());

        try {
            final User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            transactionManager.commit(transactionStatus);
        } catch (final Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account)
                .orElseThrow(UserNotFoundException::new);
    }
}
