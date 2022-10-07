package com.techcourse.repository;

import nextstep.jdbc.JdbcTemplate;

import javax.sql.DataSource;

public class UserJdbcTemplate extends JdbcTemplate {

    public UserJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected DataSource getDataSource() {
        return this.dataSource;
    }
}
