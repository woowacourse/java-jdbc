package com.techcourse.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var query = "insert into users (account, password, email) values (?, ?, ?)";
        Object[] parameters = {user.getAccount(), user.getPassword(), user.getEmail()};

        jdbcTemplate.executeUpdate(query, parameters);
    }

    public void update(final User user) {
        final var query = "update users set account = ?, password = ?, email = ?";
        Object[] parameters = {user.getAccount(), user.getPassword(), user.getEmail()};

        jdbcTemplate.executeUpdate(query, parameters);
    }

    public List<User> findAll() {
        final var query = "select id, account, password, email from users";

        return jdbcTemplate.executeQuery(query, this::mapUserFromResultSet);
    }

    public User findById(final Long id) {
        final var query = "select id, account, password, email from users where id = ?";
        Object[] parameters = {id};

        List<User> users = jdbcTemplate.executeQuery(query, this::mapUserFromResultSet, parameters);
        return users.stream()
                .findAny()
                .orElse(null);
    }

    public User findByAccount(final String account) {
        final var query = "select * from users where account = ?";
        Object[] parameters = {account};

        List<User> users = jdbcTemplate.executeQuery(query, this::mapUserFromResultSet, parameters);
        return users.stream()
                .findAny()
                .orElse(null);
    }

    private User mapUserFromResultSet(final ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    }
}
