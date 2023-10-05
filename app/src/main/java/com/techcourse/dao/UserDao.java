package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.util.List;
import java.util.NoSuchElementException;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Connection con, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(con,sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(Connection con,final User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(con,sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());

    }

    public List<User> findAll(Connection con) {
        String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(con,sql, getUserRowMapper());
    }


    public User findById(Connection con,final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(con,sql, getUserRowMapper(), id)
                .orElseThrow(() -> new NoSuchElementException("결과가 존재하지 않습니다"));
    }

    public User findByAccount(Connection con,final String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(con,sql, getUserRowMapper(), account)
                .orElseThrow(() -> new NoSuchElementException("결과가 존재하지 않습니다"));
    }

    private RowMapper<User> getUserRowMapper() {
        return (rs) ->
                new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
    }
}
