package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
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
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }
        }
    }

    public void update(User user) {
        final String sql = "update users SET account = ?, password = ?, email = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

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
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            List<User> users = new LinkedList<>();
            while (rs.next()) {
                User user = new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }
        }
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
                log.error(ignored.getMessage(), ignored);
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }
        }
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
                log.error(ignored.getMessage(), ignored);
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
                log.error(ignored.getMessage(), ignored);
            }
        }
    }
}
