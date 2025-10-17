package com.techcourse.dao;

import com.interface21.jdbc.QueryResultMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final QueryResultMapper<User> userMapper = resultSet ->
            new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user, final Connection connection) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(
                connection,
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user, final Connection connection) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(
                connection,
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll(final Connection connection) {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.queryForList(connection, sql, userMapper).stream()
                .map(result -> (User) result)
                .toList();
    }

    public User findById(final Long id, final Connection connection) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(connection, sql, userMapper, id);
    }

    public User findByAccount(final String account, final Connection connection) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(connection, sql, userMapper, account);
    }
}
