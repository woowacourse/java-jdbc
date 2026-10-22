package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import java.util.List;

public abstract class JdbcOperationTemplate {

    private final JdbcTemplate jdbcTemplate;

    protected JdbcOperationTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void update() {
        jdbcTemplate.update(createQuery(), createParameters());
    }

    public final <T> List<T> query(final RowMapper<T> rowMapper) {
        return jdbcTemplate.query(createQuery(), rowMapper, createParameters());
    }

    public final <T> T queryForObject(final RowMapper<T> rowMapper) {
        return jdbcTemplate.queryForObject(createQuery(), rowMapper, createParameters());
    }

    protected abstract String createQuery();

    protected Object[] createParameters() {
        return new Object[0];
    }
}
