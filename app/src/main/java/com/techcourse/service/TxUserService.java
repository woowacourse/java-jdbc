package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService{

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        Executable<User> insert = () -> userService.findById(id);
        return execute(insert);
    }

    @Override
    public void insert(User user) {
        Executable<Void> insert = () -> {
            userService.insert(user);
            return null;
        };
        execute(insert);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Executable<Void> changePassword = () -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        };
        execute(changePassword);
    }

    public <T> T execute(Executable<T> executable) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            T result = executable.execute();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
