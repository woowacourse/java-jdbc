package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final JdbcTemplate jdbcTemplate;

    public UserService(final JdbcTemplate jdbcTemplate) {
        this.userDao = new UserDao(jdbcTemplate);
        this.userHistoryDao = new UserHistoryDao(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void edit(final User user, final String createBy) {
        final var transactionManager = new DataSourceTransactionManager(jdbcTemplate.getDataSource());
        final var definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        final var transactionStatus = transactionManager.getTransaction(definition);

        try {
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        transactionManager.commit(transactionStatus);
    }
}
