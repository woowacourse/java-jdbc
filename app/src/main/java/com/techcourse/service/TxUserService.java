package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
        this.userService = new AppUserService(userDao, userHistoryDao);
    }

    public TxUserService(final PlatformTransactionManager transactionManager, final UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    public User findById(final long id) {
        return userService.findById(id);
    }

    public void insert(final User user) {
        startTransaction(() -> userService.insert(user));
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        startTransaction(() -> userService.changePassword(id, newPassword, createBy));
    }

    private void startTransaction(final Transactional transactional) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            transactional.start();
            transactionManager.commit(transactionStatus);
        } catch (final DataAccessException e) {
            log.error(e.getMessage());
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }
}
