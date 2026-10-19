package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, UserDao::mapUser);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return findOne(sql, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return findOne(sql, account);
    }

    private User findOne(final String sql, final Object parameter) {
        final var users = jdbcTemplate.query(sql, UserDao::mapUser, parameter);
        return users.isEmpty() ? null : users.get(0);
    }

    private static User mapUser(final ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));
    }
}
