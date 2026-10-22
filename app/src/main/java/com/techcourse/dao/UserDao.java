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
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
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
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update();
    }

    public void update(final User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set password = ? where id = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getPassword());
                pstmt.setLong(2, user.getId());
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.getDataSource();
            }
        };

        jdbcTemplate.update();
    }


    public List<User> findAll() {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {}

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        return (List<User>) jdbcTemplate.query();
    }

    public User findById(final Long id) {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setObject(1, id);
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        List<User> result = (List<User>) jdbcTemplate.query();

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    public User findByAccount(final String account) {
        SelectJdbcTemplate jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected Object mapRow(ResultSet rs) throws SQLException {
                return new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setObject(1, account);
            }

            @Override
            protected DataSource getDataSource() {
                return UserDao.this.dataSource;
            }
        };

        List<User> result = (List<User>) jdbcTemplate.query();

        if (result.isEmpty()) {
            return null;
        }

        return result.getFirst();
    }

    private DataSource getDataSource() {
        return dataSource;
    }
}
