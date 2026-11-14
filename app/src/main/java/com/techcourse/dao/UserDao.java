package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final String UPDATE_SQL = "update users set account = ?, password = ?, email = ? where id = ?";
    private static final String FIND_BY_ID_SQL = "select id, account, password, email from users where id = ?";

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
        jdbcTemplate.update(UPDATE_SQL, updateParameters(user));
    }

    public void update(final Connection connection, final User user) {
        jdbcTemplate.update(connection, UPDATE_SQL, updateParameters(user));
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, this::mapRow);
    }

    public User findById(final Long id) {
        return findUser(jdbcTemplate.query(FIND_BY_ID_SQL, this::mapRow, id));
    }

    public User findById(final Connection connection, final Long id) {
        return findUser(jdbcTemplate.query(connection, FIND_BY_ID_SQL, this::mapRow, id));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return findUser(jdbcTemplate.query(sql, this::mapRow, account));
    }

    private Object[] updateParameters(final User user) {
        return new Object[]{user.getAccount(), user.getPassword(), user.getEmail(), user.getId()};
    }

    private User findUser(final List<User> users) {
        if (users.isEmpty()) {
            return null;
        }
        return users.getFirst();
    }

    private User mapRow(final ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));
    }
}
