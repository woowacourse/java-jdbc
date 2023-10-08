package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.TransactionManager;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet ->
            new User(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4)
            );

    private final TransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final TransactionManager transactionManager, final JdbcTemplate jdbcTemplate) {
        this.transactionManager = transactionManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        transactionManager.save(
                (connection, entity) -> jdbcTemplate.executeUpdate(
                        connection,
                        sql,
                        user.getAccount(),
                        user.getPassword(),
                        user.getEmail()
                ), user);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        transactionManager.save(
                (connection, entity) -> jdbcTemplate.executeUpdate(
                        connection,
                        sql,
                        user.getAccount(),
                        user.getPassword(),
                        user.getEmail(),
                        user.getId()
                ), user);
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.executeUpdate(
                connection,
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return transactionManager.find((connection, parameters) ->
                jdbcTemplate.executeQueryForList(connection, sql, USER_ROW_MAPPER));
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return transactionManager.find(
                (connection, parameters) ->
                        jdbcTemplate.executeQueryForObject(connection, sql, USER_ROW_MAPPER, parameters), id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return transactionManager.find((connection, parameters) ->
                jdbcTemplate.executeQueryForObject(connection, sql, USER_ROW_MAPPER, parameters), account);
    }
}
