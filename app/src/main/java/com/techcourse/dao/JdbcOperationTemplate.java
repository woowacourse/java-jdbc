package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;

public abstract class JdbcOperationTemplate {

    private final JdbcTemplate jdbcTemplate;

    protected JdbcOperationTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void update() {
        jdbcTemplate.update(createQuery(), createParameters());
    }

    protected abstract String createQuery();

    protected abstract Object[] createParameters();
}
