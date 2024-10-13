package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);
    private final AppUserService appUserService;

    public TxUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        TransactionSynchronizationManager.bindAndStartTransaction(dataSource);
        try {
            appUserService.changePassword(id, newPassword, createBy);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            TransactionSynchronizationManager.unbindAndRollback(dataSource);
            throw new DataAccessException(e);
        }
        TransactionSynchronizationManager.unbindAndCommit(dataSource);
    }
}
