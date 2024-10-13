package com.techcourse.dao;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> {
        long id = resultSet.getLong("id");
        String account = resultSet.getString("account");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        return new User(id, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };
        jdbcTemplate.update(sql, preparedStatementSetter);
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? WHERE id = ?";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };
        jdbcTemplate.update(sql, preparedStatementSetter);
    }

    public void update(Connection connection, User user) {
        String sql = "update users set account = ?, password = ?, email = ? WHERE id = ?";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };
        jdbcTemplate.update(connection, sql, preparedStatementSetter);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> pstmt.setLong(1, id);
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, preparedStatementSetter);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> pstmt.setString(1, account);
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, preparedStatementSetter);
    }
}
