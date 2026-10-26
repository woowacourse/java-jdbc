package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionalProxy;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.UserService;
import com.techcourse.service.UserServiceImpl;

import javax.sql.DataSource;

public final class UserServiceConfig {

    private UserServiceConfig() {}

    public static UserService userService(final DataSource dataSource) {
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        final var target = new UserServiceImpl(new UserDao(jdbcTemplate), new UserHistoryDao(jdbcTemplate));
        return TransactionalProxy.create(UserService.class, target, dataSource);
    }
}
