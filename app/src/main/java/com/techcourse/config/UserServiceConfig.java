package com.techcourse.config;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;

public class UserServiceConfig {

    private static final UserService INSTANCE = createUserService();

    private UserServiceConfig() {}

    public static UserService getInstance() {
        return INSTANCE;
    }

    private static UserService createUserService() {
        final var dataSource = DataSourceConfig.getInstance();
        final var userDao = new UserDao(dataSource);
        final var userHistoryDao = new UserHistoryDao(dataSource);
        return new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);
    }
}
