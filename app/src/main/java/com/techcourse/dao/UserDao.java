package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            log.debug("query : {}", sql);
            try (final var rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (NullPointerException e) {
            throw new IllegalStateException("DataSource가 설정되지 않았습니다.");
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
