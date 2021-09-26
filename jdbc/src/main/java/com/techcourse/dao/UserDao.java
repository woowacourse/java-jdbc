package com.techcourse.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.SelectJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, user.getAccount());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setString(3, user.getEmail());
            }
        };
        jdbcTemplate.executeQuery();
    }

    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public String createQuery() {
                return "update users set password = ? where id = ?";
            }

            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, user.getPassword());
                preparedStatement.setLong(2, user.getId());
            }
        };
        jdbcTemplate.executeQuery();
    }

    public List<User> findAll() {

        SelectJdbcTemplate selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
            }

            @Override
            public Object mapRow(ResultSet resultSet) throws SQLException {
                return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                );
            }
        };
        return (List<User>) selectJdbcTemplate.query();
    }

    public User findById(Long id) {

        SelectJdbcTemplate selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, id);
            }

            @Override
            public Object mapRow(ResultSet resultSet) throws SQLException {
                return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                );
            }
        };

        return (User) selectJdbcTemplate.queryForObject();
    }

    public User findByAccount(String account) {

        SelectJdbcTemplate selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }

            @Override
            public String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, account);
            }

            @Override
            public Object mapRow(ResultSet resultSet) throws SQLException {
                return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)
                );
            }
        };
        return (User) selectJdbcTemplate.queryForObject();
    }
}
