package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.handleQuery(sql, (final PreparedStatement pstmt) -> {
            try {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";

        jdbcTemplate.handleQuery(sql, (final PreparedStatement pstmt) -> {
            try {
                pstmt.setString(1, user.getPassword());
                pstmt.setLong(2, user.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        List<User> list = new ArrayList<>();
        jdbcTemplate.handleQueryAndGet(sql,
                (final PreparedStatement pstmt) -> {
                },
                (final ResultSet rs) -> {
                    try {
                        while (rs.next()) {
                            list.add(new User(
                                    rs.getLong(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4)));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                });
        return list;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return (User) jdbcTemplate.handleQueryAndGet(sql,
                (final PreparedStatement pstmt) -> {
                    try {
                        pstmt.setLong(1, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                (final ResultSet rs) -> {
                    try {
                        if (rs.next()) {
                            return new User(
                                    rs.getLong(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                });
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return (User) jdbcTemplate.handleQueryAndGet(sql,
                (final PreparedStatement pstmt) -> {
                    try {
                        pstmt.setString(1, account);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                (final ResultSet rs) -> {
                    try {
                        if (rs.next()) {
                            return new User(
                                    rs.getLong(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                });
    }
}
