package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.KeyHolder;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> OBJECT_MAPPER = (ResultSet rs) ->
            new User(rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"));

    private JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Connection conn, final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        KeyHolder keyHolder = new KeyHolder();
        jdbcTemplate.update(conn, sql, keyHolder, user.getAccount(), user.getPassword(), user.getEmail());
        user.setId(keyHolder.getKey());
    }

    public void update(final Connection conn, final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(conn, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll(final Connection conn) {
        final String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(conn, OBJECT_MAPPER, sql);
    }

    public User findById(final Connection conn, final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(conn, OBJECT_MAPPER, sql, id);
    }

    public User findByAccount(final Connection conn, final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(conn, OBJECT_MAPPER, sql, account);
    }
}
