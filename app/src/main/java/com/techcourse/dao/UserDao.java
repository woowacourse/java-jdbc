package com.techcourse.dao;

import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final DataSource dataSource;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDao(final com.interface21.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void insert(final User user) {
        final var jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
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

    public void update(final User user) {
        final var jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
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
        final var selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) {
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                final List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)));
                }
                return users;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return (List<User>) selectJdbcTemplate.query();
    }

    public User findById(final Long id) {
        final var selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return (User) selectJdbcTemplate.query();
    }

    public User findByAccount(final String account) {
        final var selectJdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }

            @Override
            protected Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
        return (User) selectJdbcTemplate.query();
    }
}
