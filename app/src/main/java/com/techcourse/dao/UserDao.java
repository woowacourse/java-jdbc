package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ResultSetParser;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final ResultSetParser<User> RESULT_SET_PARSER = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Connection connection, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.queryAndGetUpdateRowsCount(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(Connection connection, final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.queryAndGetUpdateRowsCount(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(),
                user.getId());
    }

    public List<User> findAll(Connection connection) {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryAndGetResults(connection, sql, RESULT_SET_PARSER);
    }

    public User findById(Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryAndGetResult(connection, sql, RESULT_SET_PARSER, id);
    }

    public User findByAccount(Connection connection, final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryAndGetResult(connection, sql, RESULT_SET_PARSER, account);
    }
}
