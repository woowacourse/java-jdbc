package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, userRowMapper());
    }

    public User findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper(), id);
    }

    public User findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper(), account);
    }

    private RowMapper<User> userRowMapper() {
        return (rs) -> {
            return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
        };
    }
}
