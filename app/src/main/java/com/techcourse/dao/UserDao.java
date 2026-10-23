package com.techcourse.dao;

import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getAccount());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getEmail());
            }
        };

        jdbcTemplate.update();
    }

    public void update(final User user) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set password = ? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, user.getPassword());
                statement.setLong(2, user.getId());
            }
        };

        jdbcTemplate.update();
    }

    public List<User> findAll() {
        final SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected Object mapRow(final ResultSet resultSet) throws SQLException {
                return mapUser(resultSet);
            }

            @Override
            protected void setValues(final PreparedStatement statement) {
            }
        };

        return (List<User>) jdbcTemplate.query();
    }

    public User findById(final Long id) {
        final SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected Object mapRow(final ResultSet resultSet) throws SQLException {
                return mapUser(resultSet);
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setObject(1, id);
            }
        };

        final List<?> users = (List<?>) jdbcTemplate.query();
        return users.isEmpty() ? null : (User) users.getFirst();
    }

    public User findByAccount(final String account) {
        final SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected Object mapRow(final ResultSet resultSet) throws SQLException {
                return mapUser(resultSet);
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, account);
            }
        };

        final List<?> users = (List<?>) jdbcTemplate.query();
        return users.isEmpty() ? null : (User) users.getFirst();
    }

    private User mapUser(final ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
