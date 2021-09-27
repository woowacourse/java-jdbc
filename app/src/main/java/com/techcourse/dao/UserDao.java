package com.techcourse.dao;

import com.techcourse.domain.User;
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

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    private PreparedStatement createPstmt(String sql, Connection conn) throws SQLException {
        return conn.prepareStatement(sql);
    }

    private Connection createConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        InsertJdbcTemplate insertJdbcTemplate = new InsertJdbcTemplate(dataSource) {

            @Override
            public Object mapUser(ResultSet resultSet) throws SQLException {
                return null;
            }

            @Override
            public String createQuery() {
                return sql;
            }

            @Override
            public void setValuesForInsert(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.executeUpdate();
            }
        };
        insertJdbcTemplate.update();
    }


    public void update(User user) {
        final String sql = "update users set password=? where id =?";
        InsertJdbcTemplate insertJdbcTemplate = new InsertJdbcTemplate(dataSource) {

            @Override
            public Object mapUser(ResultSet resultSet) throws SQLException {
                return null;
            }

            @Override
            public String createQuery() {
                return sql;
            }

            @Override
            public void setValuesForInsert(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getPassword());
                pstmt.setLong(2, user.getId());
                pstmt.executeUpdate();
            }
        };
        insertJdbcTemplate.update();
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = createConnection();
            pstmt = createPstmt(sql, conn);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(
                        new User(
                                rs.getLong(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getString(4))
                );
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

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        InsertJdbcTemplate insertJdbcTemplate = new InsertJdbcTemplate(dataSource) {
            @Override
            public String createQuery() {
                return sql;
            }

            @Override
            public void setValuesForInsert(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            public Object mapUser(ResultSet resultSet) throws SQLException {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getLong(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4));
                }
                throw new SQLException();
            }
        };
        return (User) insertJdbcTemplate.query();
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = createConnection();
            pstmt = createPstmt(sql, conn);
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
}
