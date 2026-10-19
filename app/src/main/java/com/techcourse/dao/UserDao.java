package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";
        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getPassword());
            pstmt.setLong(2, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, pstmt -> {}, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, pstmt -> pstmt.setLong(1, id), USER_ROW_MAPPER);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, pstmt -> pstmt.setString(1, account), USER_ROW_MAPPER);
    }
}
