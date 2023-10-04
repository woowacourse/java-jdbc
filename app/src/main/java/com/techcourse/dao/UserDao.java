package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

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
        final String sql = "update users set password = ?, email = ? where id = ?";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> {
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getEmail());
            pstmt.setLong(3, user.getId());
        };
        jdbcTemplate.update(sql, preparedStatementSetter);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> pstmt.setLong(1, id);
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, preparedStatementSetter);

    }


    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> pstmt.setString(1, account);
        return jdbcTemplate.query(sql, USER_ROW_MAPPER, preparedStatementSetter);
    }
}
