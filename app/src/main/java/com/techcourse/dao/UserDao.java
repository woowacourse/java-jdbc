package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> {
        long id = resultSet.getLong("id");
        String account = resultSet.getString("account");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        return new User(id, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Connection connection, final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeWithTransaction(
                connection,
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(Connection connection, final User user) {
        String sql = "update users set account=?, password=?, email=? where id = ?";
        jdbcTemplate.executeWithTransaction(
                connection,
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
