package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var affectedRows = new AbstractJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final User user, final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getAccount());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getEmail());
            }
        }.update(user);
        log.debug("User inserted, affectedRows={}", affectedRows);
    }

    public void update(final User user) {
        final var affectedRows = new AbstractJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final User user, final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getAccount());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getEmail());
                statement.setLong(4, user.getId());
            }
        }.update(user);
        if (affectedRows == 0) {
            throw new NoSuchElementException("User not found, userId=" + user.getId());
        }
        log.debug("User updated, userId={}, affectedRows={}", user.getId(), affectedRows);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        final var users = jdbcTemplate.query(sql, USER_ROW_MAPPER);
        log.debug("Users found, count={}", users.size());
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final var user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
        log.debug("User lookup by id, userId={}, found={}", id, user != null);
        return user;
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final var user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
        log.debug("User lookup by account, found={}", user != null);
        return user;
    }
}
