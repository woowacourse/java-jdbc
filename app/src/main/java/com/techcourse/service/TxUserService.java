package com.techcourse.service;

import java.sql.Connection;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import nextstep.jdbc.DataAccessException;

public class TxUserService implements UserService {

    private final UserService userService;
    private final PlatformTransactionManager transactionManager;

    public TxUserService(PlatformTransactionManager transactionManager, UserService userService) {
        this.userService = userService;
        this.transactionManager = transactionManager;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        doProcess(() -> {
            userService.insert(user);
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        doProcess(() -> {
            userService.changePassword(id, newPassword, createBy);
        });
    }

    public void doProcess(Runnable function) {
        final TransactionStatus transactionStatus =
                transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            function.run();
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException("트랜잭션이 실패하여 롤백되었습니다.");
        } finally {
            final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
        }
    }
}
