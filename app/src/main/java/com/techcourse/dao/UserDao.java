package com.techcourse.dao;

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
                "update users set account=?, password=?, email=? where id=?",
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.queryMany(
                "select id, account, password, email from users",
                EXTRACTOR
        );
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryOne(
                "select id, account, password, email from users where id = ?",
                EXTRACTOR,
                id
        );
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryOne(
                "select id, account, password, email from users where account = ?",
                EXTRACTOR,
                account
        );
    }
}
