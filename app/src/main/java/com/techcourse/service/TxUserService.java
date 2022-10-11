package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final JdbcTemplate jdbcTemplate, final UserService userService) {
        this.transactionManager = new DataSourceTransactionManager(jdbcTemplate.getDataSource());
        this.userService = userService;
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        User user = findById(id);
        user.changePassword(newPassword);

        try {
            userService.changePassword(id, newPassword, createBy);
            transactionManager.commit(status);

        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
