package com.techcourse.config;

import java.util.Objects;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;

public class JdbcTemplateConfig {

    private static JdbcTemplate INSTANCE;

    public static JdbcTemplate getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcTemplate();
        }
        return INSTANCE;
    }

    private static JdbcTemplate createJdbcTemplate() {
        return new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return DataSourceConfig.getInstance();
            }
        };
    }

    private JdbcTemplateConfig() {}
}
