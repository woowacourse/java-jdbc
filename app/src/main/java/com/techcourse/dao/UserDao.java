package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

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
        // todo
        return null;
    }

    public User findById(final Long id) {
        final String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, account);
    }
}
