package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> ROW_MAPPER = rs -> new User(rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Connection connection, final User user) {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";

        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection connection, final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll(final Connection connection) {
        final var sql = "SELECT * FROM users";

        return jdbcTemplate.query(connection, sql, ROW_MAPPER);
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        return jdbcTemplate.queryForObject(connection, sql, ROW_MAPPER, id);
    }

    public User findByAccount(final Connection connection, final String account) {
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";

        return jdbcTemplate.queryForObject(connection, sql, ROW_MAPPER, account);
    }
}
