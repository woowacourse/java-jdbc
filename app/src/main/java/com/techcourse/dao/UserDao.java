package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import java.util.Map;
import nextstep.jdbc.JdbcTemplate;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.updateQuery(sql, Map.of(
                1, user.getAccount(),
                2, user.getPassword(),
                3, user.getEmail()
        ));
    }

    public void update(final User user) {
        String sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.updateQuery(sql, Map.of(
                1, user.getAccount(),
                2, user.getPassword(),
                3, user.getEmail(),
                4, user.getId()
        ));
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, Map.of(1, id), new UserRowMapper());
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, Map.of(1, account), new UserRowMapper());
    }

    public void deleteAll() {
        String sql = "truncate table users restart identity";
        jdbcTemplate.updateQuery(sql);
    }
}
