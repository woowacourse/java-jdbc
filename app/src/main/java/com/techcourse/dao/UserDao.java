package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = """
                INSERT INTO users (account, password, email)
                VALUES (?, ?, ?)
                """;
        jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                """;
        return jdbcTemplate.findAll(sql, (rs) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
        ));
    }

    public User findById(final Long id) {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                WHERE id = ?
                """;
        return jdbcTemplate.findById(sql, (rs) -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
        ), id);
    }

    public User findByAccount(final String account) {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                WHERE account = ?
                """;
        return jdbcTemplate.queryForObject(sql, (rs) -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        ), account);
    }
}
