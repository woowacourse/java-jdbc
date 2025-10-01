package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public SelectJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> rowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    public List<User> findAll(final UserDao userDao) {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findByAccount(final String account, final UserDao userDao) {
        final var sql = "select id, account, password, email from users where account = :account";
        final Map<String, Object> params = createParamsForFindByAccount(account);
        return jdbcTemplate.queryForObject(sql, rowMapper, params);
    }

    private Map<String, Object> createParamsForFindByAccount(final String account) {
        final Map<String, Object> params = new HashMap<>();
        params.put("account", account);
        return params;
    }
}