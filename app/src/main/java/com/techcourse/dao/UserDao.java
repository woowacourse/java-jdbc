package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.insertInto("users")
                .value("account", user.getAccount())
                .value("password", user.getPassword())
                .value("email", user.getEmail())
                .execute();
    }

    public void update(final User user) {
        jdbcTemplate.update("users")
                .set("password", user.getPassword())
                .set("email", user.getEmail())
                .where("account", user.getAccount())
                .execute();
    }

    public List<User> findAll() {
        return jdbcTemplate.select(User.class)
                .columns("id", "account", "password", "email")
                .from("users")
                .toList();
    }

    public Optional<User> findById(final Long id) {
        return jdbcTemplate.select(User.class)
                .columns("id", "account", "password", "email")
                .from("users")
                .where("id", id)
                .findFirst();
    }

    public Optional<User> findByAccount(final String account) {
        return jdbcTemplate.select(User.class)
                .columns("id", "account", "password", "email")
                .from("users")
                .where("account", account)
                .findFirst();
    }
}
