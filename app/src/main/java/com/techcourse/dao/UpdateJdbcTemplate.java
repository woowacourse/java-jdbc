package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class UpdateJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public UpdateJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void update(final User user) {
        jdbcTemplate.update(createQuery(), createParameters(user));
    }

    private String createQuery() {
        return "update users set account = ?, password = ?, email = ? where id = ?";
    }

    private Object[] createParameters(final User user) {
        return new Object[]{user.getAccount(), user.getPassword(), user.getEmail(), user.getId()};
    }
}
