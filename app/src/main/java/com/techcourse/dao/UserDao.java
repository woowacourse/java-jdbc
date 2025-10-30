package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.NoSuchElementException;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userMapper = rs -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
    );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, setter -> {
            setter.setString(1, user.getAccount());
            setter.setString(2, user.getPassword());
            setter.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        jdbcTemplate.update(sql, setter -> {
            setter.setString(1, user.getAccount());
            setter.setString(2, user.getPassword());
            setter.setString(3, user.getEmail());
            setter.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        final String sql = "SELECT * FROM users";

        return jdbcTemplate.queryForObjects(
                sql,
                setter -> {},
                userMapper
        );
    }

    public User findById(final Long id) {
        final String sql = "SELECT * FROM users WHERE id = ?";

        return jdbcTemplate.queryForObject(
                sql,
                setter -> setter.setLong(1, id),
                userMapper
        ).orElseThrow(() ->
                new NoSuchElementException("[ERROR] no such user id: " + id)
        );
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT * FROM users WHERE account = ?";

        return jdbcTemplate.queryForObject(
                sql,
                setter -> setter.setString(1, account),
                userMapper
        ).orElseThrow(() ->
                new NoSuchElementException("[ERROR] no such user account: " + account)
        );
    }
}
