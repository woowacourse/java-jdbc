package com.techcourse.dao;

import com.interface21.dao.DataNotFoundException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final RowMapper<User> ROW_MAPPER = resultSet -> {
        long id = resultSet.getLong("id");
        String account = resultSet.getString("account");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        return new User(id, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "UPDATE users SET account=?, password=?, email=? WHERE id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.queryForList(sql, ROW_MAPPER);
    }

    public Optional<User> findById(Long id) {
        try {
            String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, id));
        } catch (DataNotFoundException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByAccount(String account) {
        try {
            String sql = "SELECT id, account, password, email FROM users WHERE account=?";
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, account));
        } catch (DataNotFoundException e) {
            return Optional.empty();
        }
    }
}
