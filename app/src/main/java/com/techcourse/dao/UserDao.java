package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        final var insertedRows = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        if (insertedRows == 0) {
            throw new DataAccessException("Failed to insert user");
        }
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        final var updatedRows = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("User with id " + user.getId() + " does not exist");
        }
    }

    public void update(final Connection connection, final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        final var updatedRows = jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        if (updatedRows == 0) {
            throw new EmptyResultDataAccessException("User with id " + user.getId() + " does not exist");
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(
                sql,
                resultSet -> {
                    List<User> users = new ArrayList<>();
                    while (resultSet.next()) {
                        users.add(new User(
                                resultSet.getLong("id"),
                                resultSet.getString("account"),
                                resultSet.getString("password"),
                                resultSet.getString("email")
                        ));
                    }
                    return users;
                });
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public Optional<User> findById(final Connection connection, final long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(connection, sql, userRowMapper, id);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, userRowMapper, account);
    }
}
