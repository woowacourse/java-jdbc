package com.techcourse.repository;

import com.techcourse.config.DataSourceConfig;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserJdbcTemplate extends JdbcTemplate {

    public UserJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public Connection getConnection() {
        try {
            return DataSourceConfig.getInstance().getConnection();
        }
        catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}
