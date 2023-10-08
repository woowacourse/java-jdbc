package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(final User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        try {
            jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        } catch (SQLException e) {
            throw new DataAccessException(e) {
            };
        }

    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        try {
            return jdbcTemplate.query(sql, getUserRowMapper());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }


    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, getUserRowMapper(), id)
                    .orElseThrow(() -> new NoSuchElementException("결과가 존재하지 않습니다"));
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public User findByAccount(final String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        try {
            return jdbcTemplate.queryForObject(sql, getUserRowMapper(), account)
                    .orElseThrow(() -> new NoSuchElementException("결과가 존재하지 않습니다"));
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private RowMapper<User> getUserRowMapper() {
        return (rs) ->
                new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
    }
}
