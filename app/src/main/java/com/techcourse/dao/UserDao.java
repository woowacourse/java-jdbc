package com.techcourse.dao;

import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";

        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        final var users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? null : users.getFirst();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        final var users = jdbcTemplate.query(sql, userRowMapper, account);
        return users.isEmpty() ? null : users.getFirst();
    }
}
