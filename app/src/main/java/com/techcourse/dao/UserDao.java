package com.techcourse.dao;

import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
    }

    public UserDao(final com.interface21.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = null;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            }
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            }
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return (List<User>) jdbcTemplate.query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) {
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(final ResultSet rs) throws SQLException {
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
        });
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }
        });
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.query(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        }, new RowMapper() {
            @Override
            public Object mapRow(final ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4));
                }
                return null;
            }
        });
    }
}
