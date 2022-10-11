package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSourceTransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = new DataSourceTransactionManager(dataSource);
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public Long insert(final User user) {
        return userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
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
            throw new DataAccessException(e);
        }
    }
}
