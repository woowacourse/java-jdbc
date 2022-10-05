package com.techcourse.dao;

import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;

public class JdbcUserDao implements UserDao {

    private static final RowMapper<User> USER_MAPPER = (ResultSet rs, int rowNum) ->
        new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
        );
    private static final Logger log = LoggerFactory.getLogger(JdbcUserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @Override
    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    @Override
    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.select(sql, USER_MAPPER);
    }

    @Override
    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final List<User> users = jdbcTemplate.select(sql, USER_MAPPER, id);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("user not found");
        }
        return users.get(0);
    }

    @Override
    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final List<User> users = jdbcTemplate.select(sql, USER_MAPPER, account);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("user not found");
        }
        return users.get(0);
    }
}
