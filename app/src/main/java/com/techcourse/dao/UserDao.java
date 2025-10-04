package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(final User user) {
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public int update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
