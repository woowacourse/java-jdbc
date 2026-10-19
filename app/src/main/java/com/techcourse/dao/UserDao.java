package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;

import java.util.List;

public class UserDao {

    private static final PreparedStatementSetter NO_PARAMETERS = statement -> {};

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, statement -> {
            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, statement -> {
            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, NO_PARAMETERS, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, statement -> statement.setLong(1, id), USER_ROW_MAPPER);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, statement -> statement.setString(1, account), USER_ROW_MAPPER);
    }
}
