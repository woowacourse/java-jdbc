package com.techcourse.dao;

import java.util.List;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapStrategy;
import com.techcourse.config.DataSourceConfig;
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
        this.jdbcTemplate = new JdbcTemplate(dataSource, DataSourceConfig.getTxConnection());
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, USER_ROW_MAP_STRATEGY);
    }

    public User findById(final Long id) {
        final String sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAP_STRATEGY, id);
    }

    public User findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAP_STRATEGY, account);
    }
}
