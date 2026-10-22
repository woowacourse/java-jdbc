package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class InsertJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public InsertJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update(createQuery(), createParameters(user));
    }

    private String createQuery() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private Object[] createParameters(final User user) {
        return new Object[]{user.getAccount(), user.getPassword(), user.getEmail()};
    }
}
