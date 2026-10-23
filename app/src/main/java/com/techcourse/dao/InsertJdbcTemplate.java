package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class InsertJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public InsertJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = createQueryForInsert();

        jdbcTemplate.update(sql, createParametersForInsert(user));
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }

    private Object[] createParametersForInsert(final User user) {
        return new Object[]{user.getAccount(), user.getPassword(), user.getEmail()};
    }
}
