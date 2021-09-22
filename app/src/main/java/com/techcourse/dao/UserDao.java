package com.techcourse.dao;

import com.techcourse.dao.jdbc.InsertJdbcTemplate;
import com.techcourse.dao.jdbc.UpdateJdbcTemplate;
import com.techcourse.domain.User;
import com.techcourse.exception.dao.FindAllException;
import com.techcourse.exception.dao.FindByAccountException;
import com.techcourse.exception.dao.FindByIdException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final InsertJdbcTemplate insertJdbcTemplate;
    private final UpdateJdbcTemplate updateJdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.insertJdbcTemplate = new InsertJdbcTemplate();
        this.updateJdbcTemplate = new UpdateJdbcTemplate();
    }

    public void insert(User user) {
        insertJdbcTemplate.insert(user, this);
    }

    public void update(User user) {
        updateJdbcTemplate.update(user, this);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<User> users = new ArrayList<>();
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            LOG.debug("query: {}", sql);

            while (rs.next()) {
                users.add(new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
                ));
            }

            return users;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new FindAllException();
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

            LOG.debug("query: {}", sql);

            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new FindByIdException();
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

            LOG.debug("query: {}", sql);

            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new FindByAccountException();
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

    public DataSource getDataSource() {
        return dataSource;
    }
}
