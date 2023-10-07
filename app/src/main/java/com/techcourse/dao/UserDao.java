package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private final RowMapper<User> rowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(final Connection conn, final User user) {
        final String sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";

        jdbcTemplate.update(conn, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryObject(sql, rowMapper, id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryObject(sql, rowMapper, account);
    }
}
