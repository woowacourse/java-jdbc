package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.Optional;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.ParameterSource;
import nextstep.jdbc.RowMapper;

import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final var parameterSource = new ParameterSource();
        parameterSource.addParam(user.getAccount());
        parameterSource.addParam(user.getPassword());
        parameterSource.addParam(user.getEmail());

        jdbcTemplate.update(sql, parameterSource);
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        final var parameterSource = new ParameterSource();
        parameterSource.addParam(user.getAccount());
        parameterSource.addParam(user.getPassword());
        parameterSource.addParam(user.getEmail());
        parameterSource.addParam(user.getId());

        jdbcTemplate.update(sql, parameterSource);
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";
        final var parameterSource = new ParameterSource();

        return jdbcTemplate.query(sql, parameterSource, rowMapper);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        final var parameterSource = new ParameterSource();
        parameterSource.addParam(id);

        return jdbcTemplate.queryForObject(sql, parameterSource, rowMapper);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        final var parameterSource = new ParameterSource();
        parameterSource.addParam(account);

        return jdbcTemplate.queryForObject(sql, parameterSource, rowMapper);
    }

    private final RowMapper<User> rowMapper = (resultSet) ->
            new User(resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));
}
