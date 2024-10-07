package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataNotFoundException;
import com.interface21.jdbc.core.JdbcTemplate;
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

public class JdbcUserDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcUserDao.class);

    private final DataSource dataSource;

    public JdbcUserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcUserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        new InsertJdbcTemplate().insert(user, this);
    }

    public void update(final User user) {
        new UpdateJdbcTemplate().update(user, this);
    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet resultSet = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String account = resultSet.getString("account");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                User user = new User(id, account, password, email);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public User findById(final Long id) {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            log.debug("query : {}", sql);

            if (!resultSet.next()) {
                throw new DataNotFoundException("데이터가 존재하지 않습니다.");
            }

            return new User(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4)
            );
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public User findByAccount(final String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account=?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account);
            ResultSet resultSet = pstmt.executeQuery();

            if (!resultSet.next()) {
                throw new DataNotFoundException("데이터가 존재하지 않습니다.");
            }
            return new User(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4)
            );
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
