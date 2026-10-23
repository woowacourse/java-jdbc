package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void command(String sql, PreparedStatementSetter setter) {
        JdbcCommand jdbcCommand = new JdbcCommand(dataSource);
        jdbcCommand.execute(sql, setter);
    }

    public void command(String sql) {
        JdbcCommand jdbcCommand = new JdbcCommand(dataSource);
        jdbcCommand.execute(sql);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        JdbcQuery<T> jdbcQuery = new JdbcQuery<>(dataSource, rowMapper);
        return jdbcQuery.execute(sql, setter);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        JdbcQuery<T> jdbcQuery = new JdbcQuery<>(dataSource, rowMapper);
        return jdbcQuery.execute(sql);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        List<T> results = query(sql, rowMapper, setter);

        if (results.isEmpty()) {
            return Optional.empty();
        }

        if (results.size() > 1) {
            throw new IllegalStateException(
                    "단건 조회 결과가 " + results.size() + "개입니다."
            );
        }

        return Optional.ofNullable(results.getFirst());
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper) {
        return queryForObject(sql, rowMapper, pstmt -> {
        });
    }

}
