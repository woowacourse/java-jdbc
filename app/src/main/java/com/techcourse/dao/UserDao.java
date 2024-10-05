package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var query = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] parameters = { user.getAccount(), user.getPassword(), user.getEmail() };
        jdbcTemplate.executeUpdate(query, parameters);
    }

    public void update(final User user) {
        final var query = "update users set account = ?, password = ?, email = ?";
        Object[] parameters = { user.getAccount(), user.getPassword(), user.getEmail() };
        jdbcTemplate.executeUpdate(query, parameters);
    }

    public List<User> findAll() {
        final var query = "select id, account, password, email from users";
        return jdbcTemplate.executeQuery(query, resultSet -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        ));
    }

    public User findById(final Long id) {
        final var query = "select id, account, password, email from users where id = ?";
        Object[] parameters = { id };
        List<User> users = jdbcTemplate.executeQuery(query, resultSet -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        ), parameters);
        return users.stream()
                .findAny()
                .orElse(null);
    }

    public User findByAccount(final String account) {
        final var query = "select * from users where account = ?";
        Object[] parameters = { account };
        List<User> users = jdbcTemplate.executeQuery(query, resultSet -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        ), parameters);
        return users.stream()
                .findAny()
                .orElse(null);
    }
}
