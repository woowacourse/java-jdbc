package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.Parameters;
import com.interface21.jdbc.core.RowMapper;

import java.util.List;

public class QueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public QueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @FunctionalInterface
    public interface ParameterSetter {
        Parameters createParameters();
    }

    public void update(String sql, ParameterSetter parameterSetter) {
        final var parameters = parameterSetter.createParameters();

        jdbcTemplate.update(sql, parameters);
    }

    public <T> List<T> query(String sql, ParameterSetter parameterSetter, RowMapper<T> rowMapper) {
        final var parameters = parameterSetter.createParameters();

        return jdbcTemplate.query(sql, parameters, rowMapper);
    }
}
