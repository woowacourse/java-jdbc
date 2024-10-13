package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ?";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
        });
    }

    public void update(final Connection connection, final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ?";
        jdbcTemplate.update(sql, connection, preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
        });
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, getUserMapper());
    }

    public User findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, getUserMapper(),
                (preparedStatement) -> preparedStatement.setLong(1, id));
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, connection, getUserMapper(),
                (preparedStatement) -> preparedStatement.setLong(1, id));
    }

    public User findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, getUserMapper(),
                preparedStatement -> preparedStatement.setString(1, account));
    }

    private RowMapper<User> getUserMapper() {
        return rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
