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

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
            pstmt.executeUpdate();

            log.debug("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            List<User> users = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                String account = rs.getString("account");
                String password = rs.getString("password");
                String email = rs.getString("email");
                User user = new User(id, account, password, email);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = createPreparedStatement(conn, sql, id);
                ResultSet rs = pstmt.executeQuery()
        ) {
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
        }
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = createPreparedStatement(conn, sql, account);
                ResultSet rs = pstmt.executeQuery()) {

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
        }
    }

    public void removeAll() {
        final String sql = "delete from users";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        int index = 1;
        for (Object arg : args) {
            if (arg instanceof Long) {
                pstmt.setLong(index, (Long) arg);
            }
            if (arg instanceof String) {
                pstmt.setString(index, (String) arg);
            }
            index += 1;
        }
        return pstmt;
    }
}
