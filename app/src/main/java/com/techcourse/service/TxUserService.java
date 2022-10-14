package com.techcourse.service;

import com.techcourse.domain.User;
import javax.sql.DataSource;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager txManager;
    private final UserService userService;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.txManager = new DataSourceTransactionManager(dataSource);
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        TransactionStatus tx = txManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.insert(user);
            txManager.commit(tx);
        } catch (DataAccessException e) {
            txManager.rollback(tx);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus tx = txManager.getTransaction(new DefaultTransactionDefinition());
        try {
            userService.changePassword(id, newPassword, createBy);
            txManager.commit(tx);
        } catch (DataAccessException e) {
            txManager.rollback(tx);
        }
    }
}
