package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.exception.MoreThanTwoResultsFoundFromOptionalMethodException;
import nextstep.jdbc.core.JdbcTemplate;
import nextstep.jdbc.core.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    public static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        final String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "UPDATE users SET account=?, password=?, email=? WHERE id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        final String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(String account) {
        final String sql = "SELECT * FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }

    public Optional<User> findOptionalByAccount(String account) {
        final String sql = "SELECT * FROM users WHERE account = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, account);
        if (users.size() < 2) {
            return Optional.of(users.get(0));
        }
        throw new MoreThanTwoResultsFoundFromOptionalMethodException(users.size());
    }
}
