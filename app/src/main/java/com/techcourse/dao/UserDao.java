package com.techcourse.dao;

import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.info("Inserting user with account: {}", user.getAccount());
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        log.info("Updating user with id: {}", user.getId());
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        log.info("Updating user with id: {}", user.getId());
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        log.info("Fetching all users");
        return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
            new User(resultSet.getLong("id"), resultSet.getString("account"), resultSet.getString("password"), resultSet.getString("email")));
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        log.info("Finding user by id: {}", id);
        return jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) ->
                new User(resultSet.getLong("id"), resultSet.getString("account"), resultSet.getString("password"), resultSet.getString("email"))
            , id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        log.info("Finding user by account: {}", account);
        return jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) ->
                new User(resultSet.getLong("id"), resultSet.getString("account"), resultSet.getString("password"), resultSet.getString("email"))
            , account);
    }
}
