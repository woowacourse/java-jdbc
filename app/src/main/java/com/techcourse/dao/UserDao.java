package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();

        jdbcTemplate.update(sql, account, password, email);
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();
        final Long id = user.getId();

        jdbcTemplate.update(sql, account, password, email, id);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        try (final Connection connection = jdbcTemplate.getDataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            final List<User> users = new ArrayList<>();
            final ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final long id = resultSet.getLong("id");
                final String account = resultSet.getString("account");
                final String password = resultSet.getString("password");
                final String email = resultSet.getString("email");
                users.add(new User(id, account, password, email));
            }

            return users;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = jdbcTemplate.getDataSource().getConnection();
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
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (final SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (final SQLException ignored) {
            }
        }
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        try (final Connection connection = jdbcTemplate.getDataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account);
            final ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
            }
            return null;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
