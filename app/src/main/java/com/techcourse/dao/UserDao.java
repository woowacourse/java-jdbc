package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class UserDao {

    private static final String INSERT_SQL =
            "insert into users (account, password, email) values (?, ?, ?)";
    private static final String UPDATE_SQL =
            "update users set account = ?, password = ?, email = ? where id = ?";
    private static final String SELECT_ALL_SQL =
            "select id, account, password, email from users";
    private static final String SELECT_BY_ID_SQL = SELECT_ALL_SQL + " where id = ?";
    private static final String SELECT_BY_ACCOUNT_SQL = SELECT_ALL_SQL + " where account = ?";

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
    }

    public void insert(final User user) {
        jdbcTemplate.update(INSERT_SQL, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.update(UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(final Connection connection, final User user) {
        jdbcTemplate.update(connection, UPDATE_SQL,
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public User findById(final Connection connection, final Long id) {
        return jdbcTemplate.queryForObject(connection, SELECT_BY_ID_SQL, this::mapUser, id);
    }

    public List<User> findAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, this::mapUser);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject(SELECT_BY_ID_SQL, this::mapUser, id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryForObject(SELECT_BY_ACCOUNT_SQL, this::mapUser, account);
    }

    private User mapUser(final ResultSet resultSet, final int rowNum) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));
    }
}
