package org.springframework.jdbc.core.test_supporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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

        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(final User user) {
        final var sql = "update users set account = ?,  password = ? , email = ? where id = ?";

        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            final ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            final List<User> users = new ArrayList<>();
            if (rs.next()) {
                final User user = extractUser(rs);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            final ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return extractUser(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        try (final Connection connection = dataSource.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, account);
            final ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return extractUser(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static User extractUser(final ResultSet rs) throws SQLException {
        return new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
        );
    }
}
