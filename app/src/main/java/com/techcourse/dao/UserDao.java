package com.techcourse.dao;

import com.techcourse.dao.Strategy.FindAllStrategy;
import com.techcourse.dao.Strategy.FindByAccountStrategy;
import com.techcourse.dao.Strategy.FindByIdStrategy;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStrategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void insert(final User user) {
        jdbcTemplate.execute("insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.execute("update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = jdbcTemplate.getConnection();
            PreparedStrategy findAllStrategy = new FindAllStrategy();
            pstmt = findAllStrategy.createStatement(conn);
            rs = pstmt.executeQuery();

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

    public User findById(final Long id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = jdbcTemplate.getConnection();
            PreparedStrategy findByIdStrategy = new FindByIdStrategy();
            pstmt = findByIdStrategy.createStatement(conn);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = jdbcTemplate.getConnection();
            PreparedStrategy findByAccountStrategy = new FindByAccountStrategy();
            pstmt = findByAccountStrategy.createStatement(conn);
            pstmt.setString(1, account);
            rs = pstmt.executeQuery();

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
}
