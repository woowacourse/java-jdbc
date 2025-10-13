package com.techcourse.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.jpa.SimpleJpa;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJpa simpleJpa;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJpa = new SimpleJpa(jdbcTemplate);
    }

    public void insert(final User user) {
        simpleJpa.insert(user);
    }

    public void update(final User user) {
        simpleJpa.update(user);
    }

    public List<User> findAll() {
        return simpleJpa.selectAll(User.class);
    }

    public User findById(final Long id) {
        List<User> users = simpleJpa.selectById(User.class, id);
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with id: " + id);
        }
        return users.isEmpty() ? null : users.getFirst();
    }

    public User findByAccount(final String account) {
        List<User> users = simpleJpa.selectByColumn(User.class, "account", account);
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with account: " + account);
        }
        return users.isEmpty() ? null : users.get(0);
    }

    public void delete(User user) {
        simpleJpa.deleteById(user.getClass(), user.getId());
    }
}
