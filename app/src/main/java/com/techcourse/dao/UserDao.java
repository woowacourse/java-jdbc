package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(final User user) {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());

        log.debug("query : {}", sql);
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET password = ? WHERE account = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getAccount());

        log.debug("query : {}", sql);
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new User(
                                rs.getLong("id"),
                                rs.getString("account"),
                                rs.getString("password"),
                                rs.getString("email")
                        )
        );
    }

    public User findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new User(
                                rs.getLong("id"),
                                rs.getString("account"),
                                rs.getString("password"),
                                rs.getString("email")
                        ),
                id);
    }

    public User findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new User(
                                rs.getLong("id"),
                                rs.getString("account"),
                                rs.getString("password"),
                                rs.getString("email")
                        ), account);
    }
}
