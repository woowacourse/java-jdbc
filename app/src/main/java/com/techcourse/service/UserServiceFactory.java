package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;

import javax.sql.DataSource;

public final class UserServiceFactory {

    private UserServiceFactory() {}

    public static UserService create(final DataSource dataSource) {
        final var userDao = new UserDao(dataSource);
        final var userHistoryDao = new UserHistoryDao(dataSource);
        final var userService = new AppUserService(userDao, userHistoryDao);
        return new TxUserService(userService, dataSource);
    }
}
