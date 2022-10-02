package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = jdbcTemplate.getDataSource();
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)));
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        }
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            pstmt.setString(1, account);
            rs = pstmt.executeQuery();

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
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
