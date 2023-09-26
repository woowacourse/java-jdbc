package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class UserDao {

    private static final UserRowMapper rowMapper = new UserRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users SET account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select * from users";

        return jdbcTemplate.queries(rowMapper, sql);
    }

    public User findById(final Long id) {
        final var sql = "select * from users where id = ?";

        return (User) jdbcTemplate.query(rowMapper, sql, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select * from users where account = ?";

        return (User) jdbcTemplate.query(rowMapper, sql, account);
    }
}
