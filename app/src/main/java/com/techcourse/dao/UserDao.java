package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "UPDATE users SET account = ?, password = ? WHERE email = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public Optional<User> findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public Optional<User> findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
