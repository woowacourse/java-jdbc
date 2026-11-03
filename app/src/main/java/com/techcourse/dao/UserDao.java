package com.techcourse.dao;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.core.AbstractJdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.core.SelectJdbcTemplate;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
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
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getAccount());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getEmail());
            }
        }.update();
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
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getAccount());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getEmail());
                statement.setLong(4, user.getId());
            }
        }.update();
        if (affectedRows == 0) {
            throw new NoSuchElementException("User not found, userId=" + user.getId());
        }
        log.debug("User updated, userId={}, affectedRows={}", user.getId(), affectedRows);
    }

    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        final var users = (List<User>) new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final PreparedStatement statement) {
            }

            @Override
            protected Object mapRow(final ResultSet resultSet) throws SQLException {
                return mapUsers(resultSet);
            }
        }.query();
        log.debug("Users found, count={}", users.size());
        return users;
    }

    @SuppressWarnings("unchecked")
    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final var users = (List<User>) new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setObject(1, id);
            }

            @Override
            protected Object mapRow(final ResultSet resultSet) throws SQLException {
                return mapUsers(resultSet);
            }
        }.query();
        final var user = singleResult(users, sql);
        log.debug("User lookup by id, userId={}, found={}", id, user != null);
        return user;
    }

    @SuppressWarnings("unchecked")
    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final var users = (List<User>) new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, account);
            }

            @Override
            protected Object mapRow(final ResultSet resultSet) throws SQLException {
                return mapUsers(resultSet);
            }
        }.query();
        final var user = singleResult(users, sql);
        log.debug("User lookup by account, found={}", user != null);
        return user;
    }

    private List<User> mapUsers(final ResultSet resultSet) throws SQLException {
        final List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(USER_ROW_MAPPER.mapRow(resultSet));
        }
        return users;
    }

    private User singleResult(final List<User> users, final String sql) {
        if (users.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("Expected at most one row but found " + users.size() + ": " + sql);
        }
        return users.isEmpty() ? null : users.getFirst();
    }
}
