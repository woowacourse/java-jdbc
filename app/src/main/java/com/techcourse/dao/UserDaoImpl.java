package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    private final DataSource dataSource;
    private final JdbcTemplate insertJdbcTemplate = new JdbcTemplate() {

        @Override
        protected String createQuery() {
            return "insert into users (account, password, email) values (?, ?, ?)";
        }

        @Override
        protected DataSource getDataSource() {
            return UserDaoImpl.this.getDataSource();
        }

        @Override
        protected void setValues(User user, PreparedStatement pstmt) throws SQLException {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
        }
    };
    private final JdbcTemplate updateJdbcTemplate = new JdbcTemplate() {
        @Override
        protected String createQuery() {
            return "update users set password = ? where id = ?";
        }

        @Override
        protected DataSource getDataSource() {
            return UserDaoImpl.this.getDataSource();
        }

        @Override
        protected void setValues(User user, PreparedStatement pstmt) throws SQLException {
            pstmt.setObject(1, user.getPassword());
            pstmt.setObject(2, user.getId());
        }
    };

    public UserDaoImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(User user) {
        insertJdbcTemplate.update(user);
    }

    @Override
    public void update(User user) {
        updateJdbcTemplate.update(user);
    }

    @Override
    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(generateUser(rs));
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(generateUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            pstmt.setString(1, account);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(generateUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private User generateUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
        );
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
