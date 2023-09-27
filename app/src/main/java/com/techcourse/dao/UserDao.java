package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

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
        execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    private void execute(String sql, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }

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
        final var sql = "update users set password = ?, email = ?, account = ? where id = ?";
        execute(sql, user.getPassword(), user.getEmail(), user.getAccount(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return query(sql, (rs, rowNum) -> new User(rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")));
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return queryForObject(sql, (rs, rowNum) -> new User(rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")), id);
    }

    private <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> query = query(sql, rowMapper, params);
        if (query.size() > 1) {
            throw new RuntimeException("too many result");
        }
        if (query.isEmpty()) {
            return null;
        }
        return query.get(0);
    }

    private <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            final List<T> results = new ArrayList<>();
            if (rs.next()) {
                results.add(rowMapper.mapRow(rs, 0));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}

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

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return queryForObject(sql, (rs, rowNum) -> new User(rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")), account);
    }
}
