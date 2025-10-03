package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource, final JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        String query = "select id, account, password, email from users";
        return jdbcTemplate.query(
                query,
                (resultSet) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
                ));
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql,
                (resultSet) -> new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                ),
                id
        ).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql,
                (resultSet) -> new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                ),
                account
        ).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
    }
}
