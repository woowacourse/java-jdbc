package com.techcourse;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;

public class AppConfig {

    public static UserService userService() {
        final var dataSource = DataSourceConfig.getInstance();

        final var userDao = new UserDao(dataSource);
        final var userHistoryDao = new UserHistoryDao(dataSource);
        final var appUserService = new AppUserService(userDao, userHistoryDao);

        return new TxUserService(appUserService, dataSource);
    }

    private AppConfig() {
    }
}
