package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.NoSuchElementException;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        });
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForObjects(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER,
                        pstmt -> pstmt.setLong(1, id))
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        try (var pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return USER_ROW_MAPPER.mapRow(rs);
                }
                throw new NoSuchElementException("User not found with id: " + id);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER,
                        pstmt -> pstmt.setString(1, account))
                .orElseThrow(() -> new NoSuchElementException("User not found with account: " + account));
    }
}
