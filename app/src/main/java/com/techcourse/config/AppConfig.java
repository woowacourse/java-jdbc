package com.techcourse.config;

import com.techcourse.dao.UserDao;
import nextstep.jdbc.JdbcTemplate;

public class AppConfig {

    private AppConfig() {
    }

    public static JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSourceConfig.getInstance());
    }

    public static UserDao userDao() {
        return new UserDao(jdbcTemplate());
    }
}
