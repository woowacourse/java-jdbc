package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        jdbcTemplate.update(
                "UPDATE users SET (account, password, email) = (?, ?, ?) WHERE id=?",
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        return getUsers("SELECT id, account, password, email FROM users");
    }

    public User findById(final Long id) {
        return getUser("SELECT id, account, password, email FROM users WHERE id = ?", id);
    }

    public User findByAccount(final String account) {
        return getUser("SELECT id, account, password, email FROM users WHERE account=?", account);
    }

    private User getUser(String sql, Object... parameters) {
        return jdbcTemplate.query(
                sql,
                getUserRowMapper(),
                parameters
        );
    }

    private List<User> getUsers(String sql, Object... parameters) {
        return jdbcTemplate.queryAll(
                sql,
                getUserRowMapper(),
                parameters
        );
    }

    private RowMapper<User> getUserRowMapper() {
        return (resultSet) -> new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );
    }
}
