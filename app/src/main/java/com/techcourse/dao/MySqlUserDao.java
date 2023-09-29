package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class MySqlUserDao implements UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> {
        final long userId = resultSet.getLong("id");
        final String account = resultSet.getString("account");
        final String password = resultSet.getString("password");
        final String email = resultSet.getString("email");
        return new User(userId, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public MySqlUserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @Override
    public Long update(final User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    @Override
    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    @Override
    public Optional<User> findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        final String sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }

}
