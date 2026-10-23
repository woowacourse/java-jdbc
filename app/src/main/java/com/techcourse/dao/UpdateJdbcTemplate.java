package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class UpdateJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public UpdateJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void update(final User user) {
        final var sql = createQueryForUpdate();

        jdbcTemplate.update(sql, createParametersForUpdate(user));
    }

    private String createQueryForUpdate() {
        return "update users set password = ? where id = ?";
    }

    private Object[] createParametersForUpdate(final User user) {
        return new Object[]{user.getPassword(), user.getId()};
    }
}
