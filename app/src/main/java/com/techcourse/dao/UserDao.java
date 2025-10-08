package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import java.util.List;
import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setObject(1, user.getAccount());
            preparedStatement.setObject(2, user.getPassword());
            preparedStatement.setObject(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setObject(1, user.getPassword());
            preparedStatement.setObject(2, user.getEmail());
            preparedStatement.setObject(3, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.queryForList(sql,
                ROW_MAPPER, preparedStatement -> {
        });
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(
                sql,
                ROW_MAPPER, preparedStatement ->
                preparedStatement.setObject(1, id));
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(
                sql,
                ROW_MAPPER, preparedStatement ->
                preparedStatement.setObject(1, account));
    }
}
