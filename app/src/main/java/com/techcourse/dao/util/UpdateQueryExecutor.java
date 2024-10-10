package com.techcourse.dao.util;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.Parameters;
import com.techcourse.domain.User;

public class UpdateQueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public UpdateQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void update(final User user) {
        final var sql = createQueryForUpdate();
        final var parameters = createParametersForUpdate(user);

        jdbcTemplate.update(sql, parameters);
    }

    private String createQueryForUpdate() {
        return "update users set account = ?, password = ?, email = ? where id = ?";
    }

    private Parameters createParametersForUpdate(final User user) {
        final var parameters = new Parameters();
        parameters.add(1, user.getAccount());
        parameters.add(2, user.getPassword());
        parameters.add(3, user.getEmail());
        parameters.add(4, user.getId());

        return parameters;
    }
}
