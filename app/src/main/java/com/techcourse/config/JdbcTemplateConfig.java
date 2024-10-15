package com.techcourse.config;

import com.interface21.jdbc.core.JdbcTemplate;

public class JdbcTemplateConfig {

    private static final JdbcTemplate INSTANCE = new JdbcTemplate(DataSourceConfig.getInstance());

    public static JdbcTemplate getInstance() {
        return INSTANCE;
    }
}
