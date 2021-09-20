package com.techcourse.config;

import nextstep.jdbc.JdbcTemplate;

public class JdbcTemplateConfig {

    private static final JdbcTemplate JDBC_TEMPLATE = new JdbcTemplate(DataSourceConfig.getInstance());

    public static JdbcTemplate jdbcTemplate() {
        return JDBC_TEMPLATE;
    }

    private JdbcTemplateConfig() {
    }
}
