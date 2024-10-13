package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = new UserRowMapper();
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Connection connection, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(connection, sql, preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
        });
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(connection, sql, preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());
        });
    }

    public List<User> findAll(final Connection connection) {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(connection, sql,
                preparedStatement -> {},
                USER_ROW_MAPPER);
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(
                connection,
                sql,
                preparedStatement -> preparedStatement.setLong(1, id),
                USER_ROW_MAPPER);
    }

    public User findByAccount(final Connection connection, final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(
                connection,
                sql,
                preparedStatement -> preparedStatement.setString(1, account),
                USER_ROW_MAPPER);
    }
}
