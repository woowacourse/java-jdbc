package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.service.AppUserService;
import com.techcourse.service.TxUserService;
import com.techcourse.service.UserService;
import javax.sql.DataSource;

public class UserServiceConfig {

    private UserServiceConfig() {}

    public static UserService create(final DataSource dataSource) {
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        final var userDao = new UserDao(jdbcTemplate);
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new AppUserService(userDao, userHistoryDao);
        return new TxUserService(dataSource, userService);
    }
}
