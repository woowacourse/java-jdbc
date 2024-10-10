package com.techcourse.dao.util;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.Parameters;
import com.techcourse.domain.User;

public class InsertQueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public InsertQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = createQueryForInsert();
        final var parameters = createParametersForInsert(user);

        jdbcTemplate.update(sql, parameters);
    }

    private String createQueryForInsert() {
        return "insert into users (account, password, email) values (?, ?, ?)";
    }


    private Parameters createParametersForInsert(final User user) {
        final var parameters = new Parameters();
        parameters.add(1, user.getAccount());
        parameters.add(2, user.getPassword());
        parameters.add(3, user.getEmail());

        return parameters;
    }
}
