package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                List.of(user.getAccount(), user.getPassword(), user.getEmail())
        );
    }

    public void update(final User user) {
        jdbcTemplate.update(
                "update users set account = ?, password = ?, email = ? where id = ?",
                List.of(user.getAccount(), user.getPassword(), user.getEmail(), user.getId())
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.selectMany(
                "select id, account, password, email from users",
                this::parseUser
        );
    }

    public User findById(final Long id) {
        return jdbcTemplate.selectOne(
                "select id, account, password, email from users where id = ?",
                List.of(id),
                this::parseUser
        ).orElse(null);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.selectMany(
                "select id, account, password, email from users where account = ?",
                List.of(account),
                this::parseUser
        ).stream().findFirst().orElse(null);
    }

    private User parseUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );
    }
}
