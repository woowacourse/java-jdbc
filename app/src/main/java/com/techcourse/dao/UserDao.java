package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import javax.sql.DataSource;
import java.util.List;
import java.sql.Connection;

public class UserDao {
    private static final RowMapper<User> USER_MAPPER = rs -> new User(
            rs.getLong("id"), rs.getString("account"), rs.getString("password"), rs.getString("email"));
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.update("update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(final Connection connection, final User user) {
        jdbcTemplate.update(connection, "update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public User findById(final Connection connection, final Long id) {
        return jdbcTemplate.queryForObject(connection,
                "select id, account, password, email from users where id = ?", USER_MAPPER, id);
    }

    public List<User> findAll() {
        return jdbcTemplate.query("select id, account, password, email from users", USER_MAPPER);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject("select id, account, password, email from users where id = ?", USER_MAPPER, id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryForObject("select id, account, password, email from users where account = ?", USER_MAPPER, account);
    }
}
