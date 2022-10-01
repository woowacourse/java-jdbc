package com.techcourse.dao;

import com.techcourse.domain.User;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Long id = jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());
        user.setId(id);
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        final String sql = "SELECT id, account, password, email FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account);
            log.debug("query : {}", sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                }
                return null;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteAll() {
        final String sql = "DELETE FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pstmt.execute();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
