package com.techcourse.dao;

import com.techcourse.dao.statement.InsertPreparedStatement;
import com.techcourse.dao.statement.UpdatePreparedStatement;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        PreparedStatementExecutor executor = new InsertPreparedStatement(user);
        jdbcTemplate.executeQuery(executor);
    }

    public void update(final User user) {
        PreparedStatementExecutor executor = new UpdatePreparedStatement(user);
        jdbcTemplate.executeQuery(executor);
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        try (Connection connection = jdbcTemplate.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String account = resultSet.getString("account");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                users.add(new User(id, account, password, email));
            }
            return users;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (Connection conn = jdbcTemplate.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            log.debug("query : {}", sql);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        try (Connection connection = jdbcTemplate.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, account);

            log.debug("query : {}", sql);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }
}
