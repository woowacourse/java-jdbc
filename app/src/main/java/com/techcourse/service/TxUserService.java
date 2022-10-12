package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.exception.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final PlatformTransactionManager transactionManager, final UserService userService) {
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
        // 트랜잭션 시작
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 비즈니스 로직 처리
            userService.changePassword(id, newPassword, createBy);

            // 트랜잭션 커밋
            transactionManager.commit(transactionStatus);
        } catch (RuntimeException e) {
            // 트랜잭션 롤백
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
