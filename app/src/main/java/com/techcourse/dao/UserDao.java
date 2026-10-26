package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    public static final String USER_UPDATE_SQL = "update users set account = ?, password = ?, email = ? where id = ?";
    public static final String SELECT_ID_ACCOUNT_PASSWORD_EMAIL_FROM_USERS_WHERE_ID = "select id, account, password, email from users where id = ?";
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection connection, final User user) {
        jdbcTemplate.update(connection, USER_UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, UserDao::createUserFromResultSet);
    }

    private static User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject(SELECT_ID_ACCOUNT_PASSWORD_EMAIL_FROM_USERS_WHERE_ID,
                UserDao::createUserFromResultSet, id);
    }

    public User findById(final Connection connection, final Long id) {
        return jdbcTemplate.queryForObject(connection,
                SELECT_ID_ACCOUNT_PASSWORD_EMAIL_FROM_USERS_WHERE_ID,
                UserDao::createUserFromResultSet, id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, UserDao::createUserFromResultSet, account);
    }
}
