package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                (preparedStatement) -> {
                    preparedStatement.setString(1, user.getAccount());
                    preparedStatement.setString(2, user.getPassword());
                    preparedStatement.setString(3, user.getEmail());
                }
        );
    }

    public void update(final User user) {
        jdbcTemplate.update(
                "UPDATE users SET (account, password, email) = (?, ?, ?) WHERE id=?",
                (preparedStatement) -> {
                    preparedStatement.setString(1, user.getAccount());
                    preparedStatement.setString(2, user.getPassword());
                    preparedStatement.setString(3, user.getEmail());
                    preparedStatement.setLong(4, user.getId());
                }
        );
    }

    public List<User> findAll() {
        return getUsers(
                "SELECT id, account, password, email FROM users",
                (preparedStatement -> {})
        );
    }

    public User findById(final Long id) {
        return getUser(
                "SELECT id, account, password, email FROM users WHERE id = ?",
                (preparedStatement -> preparedStatement.setLong(1, id))
        );
    }

    public User findByAccount(final String account) {
        return getUser(
                "SELECT id, account, password, email FROM users WHERE account=?",
                (preparedStatement -> preparedStatement.setString(1, account))
        );
    }

    private User getUser(String sql, PreparedStatementSetter preparedStatementSetter) {
        return jdbcTemplate.query(
                sql,
                getUserRowMapper(),
                preparedStatementSetter
        );
    }

    private List<User> getUsers(String sql, PreparedStatementSetter preparedStatementSetter) {
        return jdbcTemplate.queryAll(
                sql,
                getUserRowMapper(),
                preparedStatementSetter
        );
    }

    private RowMapper<User> getUserRowMapper() {
        return (resultSet) -> new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );
    }
}
