package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
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
        final var sql = "select id, account, password, email from users";

        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return query(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            log.debug("query : {}", sql);
            return query(pstmt).get(0); //  TODO: IndexOutOfBoundsException 대응되도록 수정 필요
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        try (final var conn = getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account);
            log.debug("query : {}", sql);
            return query(pstmt).get(0);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
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

    private List<User> query(PreparedStatement pstmt) throws SQLException {
        try (final var rs = pstmt.executeQuery()) {
            List<User> users = new ArrayList<>();
            if (rs.next()) {
                users.add(rowMapper.mapRow(rs));
            }
            return users;
        }
    }

    private final RowMapper<User> rowMapper = (resultSet) ->
            new User(resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));
}
