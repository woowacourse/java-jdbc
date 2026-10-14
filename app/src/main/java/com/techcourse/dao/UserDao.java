package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        return jdbcTemplate.select(
                "select id, account, password, email from users",
                resultSet -> {
                    List<User> users = new ArrayList<>();
                    while (resultSet.next()) {
                        users.add(parseUser(resultSet));
                    }
                    return List.copyOf(users);
                }
        );
    }

    public User findById(final Long id) {
        return jdbcTemplate.select(
                "select id, account, password, email from users where id = ?",
                List.of(id),
                resultSet -> {
                    if (!resultSet.next()) {
                        return null;
                    }

                    return parseUser(resultSet);
                }
        );
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.select(
                "select id, account, password, email from users where account = ?",
                List.of(account),
                resultSet -> {
                    if (!resultSet.next()) {
                        return null;
                    }

                    return parseUser(resultSet);
                }
        );
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
