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

    public void insert(User user) throws SQLException {
        final String sql = createQueryForInsert();

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        try (conn; pstmt) {
            log.debug("query : {}", sql);

            setValuesForInsert(user, pstmt);
            pstmt.executeUpdate();
        }
    }

    private String createQueryForInsert() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return sql;
    }

    private void setValuesForInsert(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
    }

    public void update(User user) throws SQLException {
        final String sql = createQueryForUpdate();

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        try (conn; pstmt) {
            log.debug("query : {}", sql);

            setValuesForUpdate(user, pstmt);
            pstmt.executeUpdate();
        }
    }

    private String createQueryForUpdate() {
        final String sql = "update users set account = ?, password = ?, email = ?  where id = ?";
        return sql;
    }

    private void setValuesForUpdate(User user, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, user.getAccount());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setLong(4, user.getId());
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        final String sql = "select id, account, password, email from users";

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        try (conn; pstmt; rs) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                users.add(new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)));
            }
            return users;
        }
    }

    public User findById(Long id) throws SQLException {
        final String sql = "select id, account, password, email from users where id = ?";

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, id);
        ResultSet rs = pstmt.executeQuery();

        try (conn; pstmt; rs) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        }
    }

    public User findByAccount(String account) throws SQLException  {
        final String sql = "select id, account, password, email from users where account = ?";

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, account);
        ResultSet rs = pstmt.executeQuery();

        try (conn; pstmt; rs) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
            }
            return null;
        }
    }
}
