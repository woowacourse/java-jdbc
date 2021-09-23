package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate();
        this.jdbcTemplate.setDataSource(dataSource);
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        this.jdbcTemplate.queryDML(sql, user.getAccount(), user.getPassword(), user.getEmail());
        log.debug("query : {}", sql);
    }

    public void update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        this.jdbcTemplate.queryDML(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        log.debug("query : {}", sql);
    }

    public List<User> findAll() {
        final String sql = "select id as users_id, account as users_account, " +
                "password as users_password, email as users_email " +
                "from users";
        log.debug("query : {}", sql);
        final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        final Map<Object, List<Map<String, Object>>> resultByUser = result.stream()
                .collect(Collectors.groupingBy(it -> it.get("USERS_ID")));

        return resultByUser.values()
                .stream()
                .map(this::mapToUser)
                .collect(Collectors.toList());
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

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

    private User mapToUser(List<Map<String, Object>> maps) {
        if (maps.size() == 0) {
            return null;
        }

        return new User(
                (Long) maps.get(0).get("users_id"),
                (String) maps.get(0).get("users_account"),
                (String) maps.get(0).get("users_password"),
                (String) maps.get(0).get("users_email")
        );
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account);
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

    public void deleteAll() {
        final String sql = "delete from users";
        jdbcTemplate.queryDML(sql);

        log.debug("query : {}", sql);
    }
}
