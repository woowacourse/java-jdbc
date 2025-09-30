package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final UserMapper userMapper = new UserMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, userMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        Optional<User> user = jdbcTemplate.queryForObject(sql, userMapper, id);
        return user.orElse(null);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        Optional<User> user = jdbcTemplate.queryForObject(sql, userMapper, account);
        return user.orElse(null);
    }

    public void deleteAll() {
        final var sql = "delete from users";
        jdbcTemplate.update(sql);
    }
}
