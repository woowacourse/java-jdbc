package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public class JdbcUserDao {
    
    private final DataSource dataSource;

    public JdbcUserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcUserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        AbstractJdbcTemplate jdbcTemplate = new AbstractJdbcTemplate(dataSource) {
            @Override
            protected void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, user.getAccount());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setString(3, user.getEmail());
            }

            @Override
            protected String createQuery() {
                return "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        jdbcTemplate.update();
    }

    public void update(final User user) {
        AbstractJdbcTemplate jdbcTemplate = new AbstractJdbcTemplate(dataSource) {
            @Override
            protected void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, user.getAccount());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setString(3, user.getEmail());
                preparedStatement.setLong(4, user.getId());
            }

            @Override
            protected String createQuery() {
                return "UPDATE users SET account=?, password=?, email=? WHERE id=?";
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        jdbcTemplate.update();
    }

    public List<User> findAll() {
        SelectJdbcTemplate jbcTemplate = new SelectJdbcTemplate(dataSource) {
            @Override
            protected void setValues(PreparedStatement preparedStatement) throws SQLException {
                /*NOOP*/
            }

            @Override
            protected String createQuery() {
                return "SELECT id, account, password, email FROM users";
            }

            @Override
            protected Object mapRow(ResultSet resultSet) throws SQLException {
                return new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return jbcTemplate.queryList().stream()
                .map(o -> (User) o)
                .toList();
    }

    public User findById(final Long id) {
        SelectJdbcTemplate jbcTemplate = new SelectJdbcTemplate(dataSource) {
            @Override
            protected void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, id);
            }

            @Override
            protected String createQuery() {
                return "SELECT id, account, password, email FROM users WHERE id = ?";
            }

            @Override
            protected Object mapRow(ResultSet resultSet) throws SQLException {
                return new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return (User) jbcTemplate.query();
    }

    public User findByAccount(final String account) {
        SelectJdbcTemplate jbcTemplate = new SelectJdbcTemplate(dataSource) {
            @Override
            protected void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, account);
            }

            @Override
            protected String createQuery() {
                return "SELECT id, account, password, email FROM users WHERE account=?";
            }

            @Override
            protected Object mapRow(ResultSet resultSet) throws SQLException {
                return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                );
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return (User) jbcTemplate.query();
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
