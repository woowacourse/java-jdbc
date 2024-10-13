package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao() {
        this.jdbcTemplate = new JdbcTemplate();
    }

    public void insert(final Connection connection, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeUpdate(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection connection, final User user) throws SQLException {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.executeUpdate(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(),
                user.getId());
    }

    public List<User> findAll(final Connection connection) {
        final var sql = "select * from users";
        return jdbcTemplate.queryForList(connection, sql, USER_ROW_MAPPER);
    }

    public Optional<User> findById(final Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ? limit 1";
        List<User> users = jdbcTemplate.queryForList(connection, sql, USER_ROW_MAPPER, id);

        return getOptionalResult(users);
    }

    public Optional<User> findByAccount(final Connection connection, final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        List<User> users = jdbcTemplate.queryForList(connection, sql, USER_ROW_MAPPER, account);

        return getOptionalResult(users);
    }

    private Optional<User> getOptionalResult(List<User> users) {
        if (users.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(users.getFirst());
    }
}
