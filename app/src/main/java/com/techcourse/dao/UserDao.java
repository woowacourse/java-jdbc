package com.techcourse.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, count) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setObject(1, user.getAccount());
            ps.setObject(2, user.getPassword());
            ps.setObject(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set account=(?), password=(?), email=(?) where id=(?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setObject(1, user.getAccount());
            ps.setObject(2, user.getPassword());
            ps.setObject(3, user.getEmail());
            ps.setObject(4, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql,
                ps -> ps.setObject(1, id),
                USER_ROW_MAPPER);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql,
                ps -> ps.setObject(1, account),
                USER_ROW_MAPPER);
    }
}
