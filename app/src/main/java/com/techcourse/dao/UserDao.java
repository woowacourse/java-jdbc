package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ResultExtractor;
import com.interface21.jdbc.dsl.DslJdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private static final ResultExtractor<User> EXTRACTOR = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final DslJdbcTemplate jdbc;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbc = new DslJdbcTemplate(jdbcTemplate);
    }

    public void insert(final User user) {
        jdbc.insert("users")
                .of("account", user.getAccount())
                .of("password", user.getPassword())
                .of("email", user.getEmail())
                .execute();
    }

    public void update(final User user) {
        jdbc.update("users")
                .set("account", user.getAccount())
                .set("password", user.getPassword())
                .set("email", user.getEmail())
                .where("id", user.getId())
                .execute();
    }

    public List<User> findAll() {
        return jdbc.select("id", "account", "password", "email")
                .from("users")
                .many(User.class);
    }

    public User findById(final Long id) {
        return jdbc.select("id", "account", "password", "email")
                .from("users")
                .where("id", id)
                .one(User.class);
    }

    public User findByAccount(final String account) {
        return jdbc.select("id", "account", "password", "email")
                .from("users")
                .where("account", account)
                .one(User.class);
    }
}
