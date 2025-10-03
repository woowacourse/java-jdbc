package com.interface21.jdbc.core;

import com.interface21.jdbc.core.exception.IncorrectResultSizeDataAccessException;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class NamedParameterJdbcTemplate {

    private final NamedParameterSqlParser namedParameterSqlParser;
    private final JdbcExecutor jdbcExecutor;

    public NamedParameterJdbcTemplate(final DataSource dataSource) {
        this.namedParameterSqlParser = new NamedParameterSqlParser();
        this.jdbcExecutor = new JdbcExecutor(dataSource);
    }

    public void update(final String sql) {
        update(sql, Map.of());
    }

    public void update(final String sql, final Map<String, Object> params) {
        ParsedSql parsed = namedParameterSqlParser.parse(sql, params);
        jdbcExecutor.executeUpdate(parsed.executableSql(), parsed.args());
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper) {
        return query(sql, mapper, Map.of());
    }

    public <T> List<T> query(final String sql, final RowMapper<T> mapper, final Map<String, Object> params) {
        ParsedSql parsed = namedParameterSqlParser.parse(sql, params);
        return jdbcExecutor.executeQuery(parsed.executableSql(), parsed.args(), mapper);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, final Map<String, Object> params) {
        List<T> results = query(sql, mapper, params);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("결과값이 1개보다 많습니다. 결과 크기: " + results.size());
        }
        return results.getFirst();
    }
}


