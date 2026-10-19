package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;

import javax.sql.DataSource;

public final class AppConfig {

    private static final AppConfig INSTANCE = new AppConfig();

    private final DataSource dataSource;
    private final UserDao userDao;

    private AppConfig() {
        this.dataSource = DataSourceConfig.getInstance();
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
