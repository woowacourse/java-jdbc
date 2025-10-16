package com.techcourse.dao;

import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.jpa.SimpleJpa;
import com.techcourse.config.JpaConfig;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJpa simpleJpa;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJpa = new SimpleJpa(jdbcTemplate, JpaConfig.BASE_PACKAGE);
    }

    public void insert(Connection connection, final User user) {
        simpleJpa.insert(connection, user);
    }

    public void update(Connection connection, final User user) {
        simpleJpa.update(connection, user);
    }

    public List<User> findAll(Connection connection) {
        return simpleJpa.selectAll(connection, User.class);
    }

    public User findById(Connection connection, final Long id) {
        List<User> users = simpleJpa.selectById(connection, User.class, id);
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with id: " + id);
        }
        return users.isEmpty() ? null : users.getFirst();
    }

    public User findByAccount(Connection connection, final String account) {
        List<User> users = simpleJpa.selectByColumn(connection, User.class, "account", account);
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with account: " + account);
        }
        return users.isEmpty() ? null : users.get(0);
    }

    public void delete(Connection connection, User user) {
        simpleJpa.deleteById(connection, user.getClass(), user.getId());
    }
}
