package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public abstract class JdbcOperationTemplate {

    private final JdbcTemplate jdbcTemplate;

    protected JdbcOperationTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void update(final User user) {
        jdbcTemplate.update(createQuery(), createParameters(user));
    }

    protected abstract String createQuery();

    protected abstract Object[] createParameters(User user);
}
