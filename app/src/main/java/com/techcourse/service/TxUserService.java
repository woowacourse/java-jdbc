package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.transaction.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionManager;

import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final TransactionManager transactionManager = new JdbcTransactionManager();
    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return transactionManager.executeAndReturn(dataSource, () -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        transactionManager.execute(dataSource, () -> userService.insert(user));
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        transactionManager.execute(dataSource, () -> userService.changePassword(id, newPassword, createBy)
        );
    }
}
