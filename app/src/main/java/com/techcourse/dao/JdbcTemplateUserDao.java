package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.dao.mapper.UserMapper;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;

public class JdbcTemplateUserDao implements UserDao {

    private static final RowMapper<User> ROW_MAPPER = new UserMapper();

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET account=?, password=?, email=? WHERE id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.queryForList(sql, ROW_MAPPER);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account=?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
    }
}
