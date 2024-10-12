package com.techcourse.dao;

import com.interface21.jdbc.core.GeneratedKeyHolder;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs -> {
        long id = rs.getLong("id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");

        return new User(id, account, password, email);
    });
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User insert(final User user) {
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, keyHolder, user.getAccount(), user.getPassword(), user.getEmail());

        long id = (long) keyHolder.getKey();
        return new User(id, user);
    }

    public void update(final Connection connection, final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getAccount(), user.getId());
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getAccount(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }

    public void deleteAll() {
        final String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }
}
