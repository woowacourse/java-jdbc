package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(
                sql,
                stmt -> {
                    stmt.setString(1, user.getAccount());
                    stmt.setString(2, user.getPassword());
                    stmt.setString(3, user.getEmail());
                }
        );
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(
                sql,
                stmt -> {
                    stmt.setString(1, user.getAccount());
                    stmt.setString(2, user.getPassword());
                    stmt.setString(3, user.getEmail());
                    stmt.setLong(4, user.getId());
                }
        );
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.select(sql,
                stmt -> {
                },
                parseToUser()
        );
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        List<User> queryResult = jdbcTemplate.select(
                sql,
                stmt -> stmt.setLong(1, id),
                parseToUser()
        );

        if (queryResult.isEmpty()) {
            return null;
        }

        return queryResult.getFirst();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        List<User> queryResult = jdbcTemplate.select(
                sql,
                stmt -> stmt.setString(1, account),
                parseToUser()
        );

        if (queryResult.isEmpty()) {
            return null;
        }

        return queryResult.getFirst();
    }

    private RowMapper<User> parseToUser() {
        return resultSet -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    }
}
