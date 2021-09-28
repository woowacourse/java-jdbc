package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "UPDATE users SET account=?, password=?, email=? WHERE id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", ((rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"))
        ));
    }

    public User findById(Long id) {
        final String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")),
                id);
    }

    public User findByAccount(String account) {
        final String sql = "SELECT * FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")),
                account);
    }
}
