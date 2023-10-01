package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final Object[] parameters = {user.getAccount(), user.getPassword(), user.getEmail()};

        jdbcTemplate.update(sql, parameters);
    }

    public void update(final User user) {
        final var sql = "update users set account = ? , password = ? , email = ? where id = ?";
        final Object[] parameters = {user.getAccount(), user.getPassword(), user.getEmail(), user.getId()};

        jdbcTemplate.update(sql, parameters);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, rawsMapper());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final Object[] parameters = {id};

        return jdbcTemplate.queryForObject(sql, rawMapper(), parameters);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final Object[] parameters = {account};

        return jdbcTemplate.queryForObject(sql, rawMapper(), parameters);
    }

    public Function<ResultSet, User> rawMapper() {
        return (resultSet ->
        {
            try {
                return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Function<ResultSet, List<User>> rawsMapper() {
        return (resultSet ->
        {
            List<User> users = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    users.add(new User(
                            resultSet.getLong(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4)
                    ));
                }
                return users;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
