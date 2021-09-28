package com.techcourse.repository;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.core.JdbcTemplate;

public class H2DataBaseConfig {
    private static final UserDao userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));

    static {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    public static UserDao getUserDao() {
        return userDao;
    }
}
