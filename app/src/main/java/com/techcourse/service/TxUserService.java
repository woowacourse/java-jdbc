package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final PlatformTransactionManager transactionManager;
    private final AppUserService appUserService;

    public TxUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        transactionManager = new DataSourceTransactionManager(DataSourceConfig.getInstance());
        this.appUserService = new AppUserService(userDao, userHistoryDao);
    }

    public TxUserService(final PlatformTransactionManager transactionManager, final AppUserService appUserService) {
        this.transactionManager = transactionManager;
        this.appUserService = appUserService;
    }

    public User findById(final long id) {
        return appUserService.findById(id);
    }

    public void insert(final User user) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            appUserService.insert(user);
            transactionManager.commit(transactionStatus);
        } catch (final DataAccessException e) {
            log.error(e.getMessage());
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            appUserService.changePassword(id, newPassword, createBy);
            transactionManager.commit(transactionStatus);
        } catch (final DataAccessException e) {
            log.error(e.getMessage());
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }
}
