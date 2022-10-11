package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final DataSourceTransactionManager transactionManager;
    private final UserHistoryDao userHistoryDao;

    public UserService(UserDao userDao, DataSource dataSource, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.transactionManager = new DataSourceTransactionManager(dataSource);
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(Long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            transactionManager.rollback(status);
        }
    }

    public void changePassword(Long id, String newPassword, String createBy) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            transactionManager.rollback(status);
        }
    }
}
