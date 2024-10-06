package com.techcourse.dao;

import java.util.List;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = (rs, rowNum) ->
            new User(
                    rs.getLong("id"), rs.getString("account"),
                    rs.getString("password"), rs.getString("email")
            );

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select * from users";
        return jdbcTemplate.queryForObjects(sql, rowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, account);
    }
}
