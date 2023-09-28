package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> {
        final long userId = rs.getLong("id");
        final String account = rs.getString("account");
        final String password = rs.getString("password");
        final String email = rs.getString("email");
        return new User(userId, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
