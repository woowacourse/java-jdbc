package com.techcourse.dao;

import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementStrategy;
import com.interface21.jdbc.core.RowMapStrategy;
import com.techcourse.domain.User;

public class UserDao {

    private static final RowMapStrategy<User> USER_ROW_MAP_STRATEGY = resultSet -> {
        final long id = resultSet.getLong(1);
        final String findAccount = resultSet.getString(2);
        final String password = resultSet.getString(3);
        final String email = resultSet.getString(4);
        return new User(id, findAccount, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        final PreparedStatementStrategy strategy = connection -> {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            return statement;
        };
        jdbcTemplate.update(strategy);
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        final PreparedStatementStrategy strategy = connection -> {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setLong(4, user.getId());
            return statement;
        };
        jdbcTemplate.update(strategy);
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";
        final PreparedStatementStrategy preparedStatementStrategy = connection -> connection.prepareStatement(sql);
        return jdbcTemplate.query(preparedStatementStrategy, USER_ROW_MAP_STRATEGY);
    }

    public User findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        final PreparedStatementStrategy preparedStatementStrategy = connection -> {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            return statement;
        };

        return jdbcTemplate.queryForObject(preparedStatementStrategy, USER_ROW_MAP_STRATEGY);
    }

    public User findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";

        final PreparedStatementStrategy preparedStatementStrategy = connection -> {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, account);
            return statement;
        };

        return jdbcTemplate.queryForObject(preparedStatementStrategy, USER_ROW_MAP_STRATEGY);
    }
}
