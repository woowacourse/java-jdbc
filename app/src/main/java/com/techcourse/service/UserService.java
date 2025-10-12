package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final TransactionManager transactionManager;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final TransactionManager transactionManager) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.transactionManager = transactionManager;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        transactionManager.start();
        try {
            userDao.insert(user);
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException("메서드 실행 중 예외가 발생하여 롤백합니다.", e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.start();
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException("메서드 실행 중 예외가 발생하여 롤백합니다.", e);
        }
    }
}
