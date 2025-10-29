package com.techcourse.dao;

import java.util.List;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.jpa.SimpleJpa;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.config.JpaConfig;
import com.techcourse.config.TransactionManagerConfig;
import com.techcourse.domain.User;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJpa simpleJpa;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJpa = new SimpleJpa(jdbcTemplate, JpaConfig.BASE_PACKAGE);
    }

    public void insert(final User user) {
        simpleJpa.insert(TransactionManagerConfig.getCurrentConnection(dataSource), user);
    }

    public void update(final User user) {
        simpleJpa.update(TransactionManagerConfig.getCurrentConnection(dataSource), user);
    }

    public List<User> findAll() {
        return simpleJpa.selectAll(TransactionManagerConfig.getCurrentConnection(dataSource),
            User.class);
    }

    public User findById(final Long id) {
        List<User> users = simpleJpa.selectById(
            TransactionManagerConfig.getCurrentConnection(dataSource), User.class, id);
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with id: " + id);
        }
        return users.isEmpty() ? null : users.getFirst();
    }

    public User findByAccount(final String account) {
        List<User> users = simpleJpa.selectByColumn(
            TransactionManagerConfig.getCurrentConnection(dataSource), User.class, "account",
            account);
        if (users.size() > 1) {
            throw new IllegalStateException("Multiple users found with account: " + account);
        }
        return users.isEmpty() ? null : users.get(0);
    }

    public void delete(User user) {
        simpleJpa.deleteById(TransactionManagerConfig.getCurrentConnection(dataSource), user.getClass(), user.getId());
    }
}
