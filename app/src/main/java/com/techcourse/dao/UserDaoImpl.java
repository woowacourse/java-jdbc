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

    public UserDaoImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParams(pstmt, user.getAccount(), user.getPassword(), user.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setParams(pstmt, user.getPassword(), user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
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
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.of(generateUser(rs));
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
            pstmt.setString(1, account);
            ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.of(generateUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
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
}
