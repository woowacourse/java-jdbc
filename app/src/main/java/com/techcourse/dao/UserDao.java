package com.techcourse.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.SelectJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private SelectJdbcTemplate selectJdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        jdbcTemplate.update();
    }

    public void update(User user) {
        jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        jdbcTemplate.update();
    }

    public List<User> findAll() {
        selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

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
        };
        return (List<User>) selectJdbcTemplate.query();
    }

    public User findById(Long id) {
        selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

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
        };
        return (User) selectJdbcTemplate.query();
    }

    public User findByAccount(String account) {
        selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

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
        };
        return (User) selectJdbcTemplate.query();
    }
}
