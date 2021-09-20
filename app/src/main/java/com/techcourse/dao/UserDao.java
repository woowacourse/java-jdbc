package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        };
        jdbcTemplate.update();
    }

    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }
        };
        jdbcTemplate.update();
    }

    public List<User> findAll() {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {

            }

            @Override
            protected Object mapRow(ResultSet resultSet) throws SQLException {
                List<User> users = new ArrayList<>();
                if (resultSet.next()) {
                    users.add(new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)));
                }
                return users;
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        return (List<User>) jdbcTemplate.query();
    }

    public User findById(Long id) {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            protected Object mapRow(ResultSet resultSet) throws SQLException {
                if (resultSet.next()) {
                    return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4));
                }
                return null;
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        return (User) jdbcTemplate.query();
    }

    public User findByAccount(String account) {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            protected Object mapRow(ResultSet resultSet) throws SQLException {
                if (resultSet.next()) {
                    return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4));
                }
                return null;
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };
        return (User) jdbcTemplate.query();
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
