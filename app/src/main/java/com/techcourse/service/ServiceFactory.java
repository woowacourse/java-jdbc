package com.techcourse.service;

import com.interface21.transaction.support.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import javax.sql.DataSource;

public class ServiceFactory {

    private static final DataSource dataSource = DataSourceConfig.getInstance();

    private ServiceFactory() {
    }

    public static UserService createUserService() {
        UserDao userDao = new UserDao(dataSource);
        UserHistoryDao userHistoryDao = new UserHistoryDao(dataSource);
        UserService appUserService = new AppUserService(userDao, userHistoryDao);
        TransactionManager transactionManager = new TransactionManager(dataSource);
        return new TxUserService(appUserService, transactionManager);
    }
}
