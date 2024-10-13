package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs -> {
        long id = rs.getLong("id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");

        return new User(id, account, password, email);
    });

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(Connection connection, User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        jdbcTemplate.update(sql, connection, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
