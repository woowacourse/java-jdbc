package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    public static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        Consumer<PreparedStatement> consumer = pstmt -> {
            try {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };

        jdbcTemplate.execute(sql, consumer);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        Consumer<PreparedStatement> consumer = pstmt -> {
            try {
                pstmt.setObject(1, user.getAccount());
                pstmt.setObject(2, user.getPassword());
                pstmt.setObject(3, user.getEmail());
                pstmt.setObject(4, user.getId());
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };

        jdbcTemplate.execute(sql, consumer);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        Consumer<PreparedStatement> consumer = pstmt -> { };

        Function<ResultSet, User> rowMapper = rs -> {
            try {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };

        return jdbcTemplate.queryForList(sql, consumer, rowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Consumer<PreparedStatement> consumer = pstmt -> {
            try {
                pstmt.setObject(1, id);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };

        Function<ResultSet, User> function = rs -> {
            try {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };

        return jdbcTemplate.queryForObject(sql, consumer, function);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        Consumer<PreparedStatement> consumer = pstmt -> {
            try {
                pstmt.setObject(1, account);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };

        Function<ResultSet, User> rowMapper = rs -> {
            try {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException();
            }
        };

        return jdbcTemplate.queryForObject(sql, consumer, rowMapper);
    }
}
