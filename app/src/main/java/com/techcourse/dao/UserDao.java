package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();

        jdbcTemplate.update(sql, account, password, email);
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();
        final Long id = user.getId();

        jdbcTemplate.update(sql, account, password, email, id);
    }

    public void update(final Connection connection, final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();
        final Long id = user.getId();

        jdbcTemplate.update(sql, connection, account, password, email, id);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
        return Optional.ofNullable(user);
    }

    public Optional<User> findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, account);
        return Optional.ofNullable(user);
    }
}
