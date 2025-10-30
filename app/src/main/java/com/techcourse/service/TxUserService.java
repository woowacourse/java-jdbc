package com.techcourse.service;

import static com.interface21.transaction.support.TransactionManager.doInReadOnlyTransaction;
import static com.interface21.transaction.support.TransactionManager.doInTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionCallback;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        dataSource = DataSourceConfig.getInstance();
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return doInReadOnlyTransaction(dataSource, () -> userService.findById(id));
    }

    @Override
    public void save(final User user) {
        doInTransaction(dataSource, () -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        doInTransaction(dataSource, () -> userService.changePassword(id, newPassword, createBy));
    }
}
