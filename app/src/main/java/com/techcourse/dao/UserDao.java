package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = """
                INSERT INTO users (account, password, email)
                VALUES (?, ?, ?)
                """;
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(final User user) {
        final var sql = """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """;
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            pstmt.setString(1, user.getPassword());
            pstmt.setLong(2, user.getId());
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                """;
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
                final ResultSet rs = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);
            final var users = new java.util.ArrayList<User>();
            while (rs.next()) {
                users.add(new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4))
                );
            }
            return users;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                WHERE id = ?
                """;
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            pstmt.setLong(1, id);
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                }
                return null;
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findByAccount(final String account) {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                WHERE account = ?
                """;
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            pstmt.setString(1, account);
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                }
                return null;
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
