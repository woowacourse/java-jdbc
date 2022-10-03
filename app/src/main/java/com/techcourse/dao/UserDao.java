package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static RowMapper<User> userMapper() {
        return (resultSet) -> {
            resultSet.next();
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );
        };
    }

    private static RowMapper<List<User>> usersMapper() {
        return (resultSet) -> {
            final var users = new ArrayList<User>();

            while (!resultSet.isLast()) {
                final var user = userMapper().mapRow(resultSet);
                users.add(user);
            }

            return users;
        };
    }

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(final User user) {
        return jdbcTemplate.command(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.query(
                "select id, account, password, email from users",
                usersMapper());
    }

    public User findById(final Long id) {
        return jdbcTemplate.query(
                "select id, account, password, email from users where id = ?",
                userMapper(),
                id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.query(
                "select id, account, password, email from users where account = ?",
                userMapper(),
                account);
    }

    public int update(final User user) {
        return jdbcTemplate.command(
                "update users set password = ? where id = ?",
                user.getPassword(), user.getId());
    }
}
