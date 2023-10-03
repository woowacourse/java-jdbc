package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UserDao {
    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final JdbcTemplate template;

    public UserDao(final JdbcTemplate template) {
        this.template = template;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        template.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";
        template.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return template.query(sql, userRowMapper());
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return template.queryForObject(sql, userRowMapper(), id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return template.queryForObject(sql, userRowMapper(), account);
    }

    private RowMapper<User> userRowMapper() {
        return USER_ROW_MAPPER;
    }
}
