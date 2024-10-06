package com.techcourse.dao;

import com.interface21.dao.DataAccessUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.rowmapper.UserRowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource; // TODO: 해당 필드 제거 하고 jdbcTemplate 필드 추가
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = null;
        this.userRowMapper = null;
    }

    public UserDao(final JdbcTemplate jdbcTemplate, final UserRowMapper userRowMapper) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);

            log.debug("query : {}", sql);

            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);

            log.debug("query : {}", sql);

            statement.setString(1, user.getAccount());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setLong(4, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public List<User> findAll() {
        final String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return DataAccessUtils.optionalResult(users);
    }

    public Optional<User> findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, account);
        return DataAccessUtils.optionalResult(users);
    }
}
