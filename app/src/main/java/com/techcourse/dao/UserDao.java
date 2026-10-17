package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, createRowMapper());
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return findOne(sql, id);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return findOne(sql, account);
    }

    private Optional<User> findOne(final String sql, final Object... args) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, createRowMapper(), args));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static RowMapper<User> createRowMapper() {
        return rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }
}
