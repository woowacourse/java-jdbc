package com.techcourse.config;

import com.interface21.transaction.interceptor.TransactionProxyFactory;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.AppUserService;
import com.techcourse.service.UserService;
import javax.sql.DataSource;

public final class UserServiceConfig {

    private UserServiceConfig() {}

    public static UserService create() {
        return create(DataSourceConfig.getInstance());
    }

    public static UserService create(final DataSource dataSource) {
        final var target = new AppUserService(new UserDao(dataSource), new UserHistoryDao(dataSource));
        return TransactionProxyFactory.create(UserService.class, target, dataSource);
    }
}
