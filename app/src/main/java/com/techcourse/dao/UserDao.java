package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.core.JdbcTemplate;
import nextstep.jdbc.support.ResultSetExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final ResultSetExtractor<User> userResultSetExtractor = resultSet -> {
        final long id = resultSet.getLong("id");
        final String account = resultSet.getString("account");
        final String password = resultSet.getString("password");
        final String email = resultSet.getString("email");
        return new User(id, account, password, email);
    };

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set password=?, email=? where account=?";
        jdbcTemplate.update(sql, user.getPassword(), user.getEmail(), user.getAccount());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, userResultSetExtractor);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userResultSetExtractor, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userResultSetExtractor, account);
    }
}
