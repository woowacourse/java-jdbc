package com.techcourse.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.insert(user);
    }

    public void update(final User user) {
        jdbcTemplate.update(user, Map.of("id", user.getId()));
    }

    public List<User> findAll() {
        return jdbcTemplate.select(User.class, Map.of());
    }

    public User findById(final Long id) {
        List<User> users = jdbcTemplate.select(User.class, Map.of("id", id));
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with id: " + id);
        }
        return users.isEmpty() ? null : users.getFirst();
    }

    public User findByAccount(final String account) {
        List<User> users = jdbcTemplate.select(User.class, Map.of("account", account));
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with account: " + account);
        }
        return users.isEmpty() ? null : users.get(0);
    }

    public void delete(User user) {
        jdbcTemplate.delete(user.getClass(), Map.of("id", user.getId()));
    }
}
