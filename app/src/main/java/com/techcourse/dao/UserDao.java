package com.techcourse.dao;

import com.interface21.jdbc.client.Sql;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ResultExtractor;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private static final ResultExtractor<User> EXTRACTOR = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final Sql sql;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.sql = new Sql(jdbcTemplate);
    }

    public void insert(final User user) {
        sql.insert("users")
                .of("account", user.getAccount())
                .of("password", user.getPassword())
                .of("email", user.getEmail())
                .execute();
    }

    public void update(final User user) {
        sql.update("users")
                .set("account", user.getAccount())
                .set("password", user.getPassword())
                .set("email", user.getEmail())
                .where("id", user.getId())
                .execute();
    }

    public List<User> findAll() {
        return sql.select("id", "account", "password", "email")
                .from("users")
                .many(EXTRACTOR);
    }

    public User findById(final Long id) {
        return sql.select("id", "account", "password", "email")
                .from("users")
                .where("id", id)
                .one(EXTRACTOR);
    }

    public User findByAccount(final String account) {
        return sql.select("id", "account", "password", "email")
                .from("users")
                .where("account", account)
                .one(EXTRACTOR);
    }
}
