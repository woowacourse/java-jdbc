package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class UserDao {

    private static final Function<ResultSet, User> RESULT_SET_TO_USER = rs -> {
        try {
            return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

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
        // todo
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryOne(
                "select id, account, password, email from users where id = ?",
                RESULT_SET_TO_USER,
                id
        );
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
