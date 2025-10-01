package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import java.util.NoSuchElementException;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userMapper = resultSet ->
        new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
                );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.queryForObjects(sql, userMapper);
    }

    public User findById(final Long id) {
        final var sql = "select * from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userMapper, id)
                .orElseThrow(
                        () -> new NoSuchElementException("[ERROR] no such user id: " + id)
                );
    }

    public User findByAccount(final String account) {
        final String sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userMapper, account)
                .orElseThrow(
                        () -> new NoSuchElementException("[ERROR] no such user account: " + account)
                );
    }
}
