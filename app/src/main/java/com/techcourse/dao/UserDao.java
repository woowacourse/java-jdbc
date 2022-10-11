package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.Optional;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

import java.util.List;

public class UserDao {

    private static final RowMapper<User> ROW_MAPPER = (resultSet) ->
            new User(resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final var account = user.getAccount();
        final var password = user.getPassword();
        final var email = user.getEmail();

        jdbcTemplate.update(sql, account, password, email);
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        final var account = user.getAccount();
        final var password = user.getPassword();
        final var email = user.getEmail();
        final var id = user.getId();

        jdbcTemplate.update(sql, account, password, email, id);
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
    }
}
