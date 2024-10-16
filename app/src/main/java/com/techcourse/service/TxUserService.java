package com.techcourse.service;


import com.interface21.jdbc.core.JdbcTemplateException;
import com.interface21.transaction.support.TransactionManager;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final TransactionManager transactionManager, final UserService userService) {
        if (userService instanceof TxUserService) {
            throw new IllegalArgumentException("트랜잭션 서비스는 자신을 참조할 수 없습니다.");
        }
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            transactionManager.beginTransaction();
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit();
        } catch (RuntimeException exception) {
            transactionManager.rollback();
            throw new JdbcTemplateException("트랜잭션을 커밋하는 데 실패하였습니다. 트랜잭션을 롤백합니다.", exception);
        } finally {
            transactionManager.closeConnection();
        }
    }
}
