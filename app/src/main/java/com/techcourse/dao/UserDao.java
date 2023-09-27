package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper mapper = new UserMapper();

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getAccount(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.queryForObject(sql, mapper, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.queryForObject(sql, mapper, account);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, mapper);
    }
}
