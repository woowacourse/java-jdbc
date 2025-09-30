package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.rowmapper.RowMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = """
            INSERT INTO users(account, password, email)
            VALUES(?, ?, ?)
        """;
        final Object[] params = new Object[]{
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        };
        jdbcTemplate.update(sql, params);
    }

    public void update(final User user) {
        final String sql = """
            UPDATE users
            SET account = ?, password = ?, email = ?
            WHERE id = ?
        """;
        final Object[] params = new Object[]{
            user.getAccount(),
            user.getPassword(),
            user.getEmail(),
            user.getId()
        };
        jdbcTemplate.update(sql, params);
    }

    public List<User> findAll() {
        final String sql = """
            SELECT id, account, password, email
            FROM users
        """;
        RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
        );
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final String sql = """
            SELECT id, account, password, email
            FROM users
            WHERE id = ?
        """;
        RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
        );
        Object[] params = new Object[]{id};
        return jdbcTemplate.queryForObject(sql, userRowMapper, params);
    }

    public User findByAccount(final String account) {
        final String sql = """
            SELECT id, account, password, email
            FROM users
            WHERE account = ?
        """;
        RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
        );
        Object[] params = new Object[]{account};
        return jdbcTemplate.queryForObject(sql, userRowMapper, params);
    }
}
