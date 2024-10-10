package com.techcourse.dao.executor;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.Parameters;

public class QueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public QueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @FunctionalInterface
    public interface ParameterSetter {
        Parameters createParameters();
    }

    public void execute(String sql, ParameterSetter parameterSetter) {
        final var parameters = parameterSetter.createParameters();

        jdbcTemplate.update(sql, parameters);
    }
}
