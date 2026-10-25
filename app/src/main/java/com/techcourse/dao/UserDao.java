package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            log.debug("query : {}", sql);
            final List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            statement.setString(1, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User mapRow(final ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));
    }
}
