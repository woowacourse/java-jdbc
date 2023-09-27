package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> ROW_MAPPER = (rs ->
            new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
            )
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(connection -> {
            try {
                final PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                return pstmt;
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(connection -> {
            try {
                final PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
                return pstmt;
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(connection -> {
            try {
                final PreparedStatement pstmt = connection.prepareStatement(sql);
                return pstmt;
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, ROW_MAPPER);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(connection -> {
            try {
                final PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setLong(1, id);
                return pstmt;
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, ROW_MAPPER);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(connection -> {
            try {
                final PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, account);
                return pstmt;
            } catch (final SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, ROW_MAPPER);
    }
}
