package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.support.ExceptionWrapper;

public class UserDao {

    private static Function<ResultSet, User> userMapper() {
        return (resultSet) -> ExceptionWrapper.get(() -> {
            // Moves the cursor forward one row from its current position.
            // A ResultSet cursor is initially positioned before the first row
            resultSet.next();
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );
        });
    }

    private static Function<ResultSet, List<User>> usersMapper() {
        return (resultSet) -> ExceptionWrapper.get(() -> {
            final var users = new ArrayList<User>();

            while (!resultSet.isLast()) {
                final var user = userMapper().apply(resultSet);
                users.add(user);
            }

            return users;
        });
    }

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(final User user) {
        return jdbcTemplate.insert(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.select(
                "select id, account, password, email from users",
                usersMapper());
    }

    public User findById(final Long id) {
        return jdbcTemplate.select(
                "select id, account, password, email from users where id = ?",
                userMapper(),
                id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.select(
                "select id, account, password, email from users where account = ?",
                userMapper(),
                account);
    }

    public int update(final User user) {
        return jdbcTemplate.insert(
                "update users set password = ? where id = ?",
                user.getPassword(), user.getId());
    }
}
