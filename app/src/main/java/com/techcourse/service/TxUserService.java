package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.TransactionManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService{

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final TransactionManager transactionManager;

    public TxUserService(UserService userService, TransactionManager transactionManager) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        transactionManager.start();
        try {
            userService.insert(user);
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException("메서드 실행 중 예외가 발생하여 롤백합니다.", e);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionManager.start();
        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit();
        } catch (Exception e) {
            transactionManager.rollback();
            throw new DataAccessException("메서드 실행 중 예외가 발생하여 롤백합니다.", e);
        }
    }
}
