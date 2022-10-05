package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        // todo
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getId(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        // todo
        final var sql = "SELECT id, account, password, email FROM users";
        final List<User> users = jdbcTemplate.query(sql, rowMapper());
        log.info("Users: {}", users);
        return users;
    }

    public Optional<User> findById(final Long id) {
        final var sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        try {
            final User user = jdbcTemplate.queryForObject(sql, rowMapper(), id);
            log.info("User: {}", user);
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByAccount(final String account) {
        // todo
        final var sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        try {
            final User user = jdbcTemplate.queryForObject(sql, rowMapper(), account);
            log.info("User: {}", user);
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<User> rowMapper() {
        return (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
