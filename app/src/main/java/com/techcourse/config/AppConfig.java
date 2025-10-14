package com.techcourse.config;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;

public class AppConfig {

    public static final AppConfig INSTANCE = new AppConfig();

    private UserService userService;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;

    private AppConfig() {
    }

    public UserService userService() {
        if (userService == null) {
            final AppUserService appUserService = new AppUserService(userDao(), userHistoryDao());
            this.userService = new TxUserService(appUserService);
        }
        return userService;
    }

    public UserDao userDao() {
        if (userDao == null) {
            this.userDao = new UserDao(DataSourceConfig.getInstance());
        }
        return userDao;
    }

    public UserHistoryDao userHistoryDao() {
        if (userHistoryDao == null) {
            this.userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        }
        return userHistoryDao;
    }
}
