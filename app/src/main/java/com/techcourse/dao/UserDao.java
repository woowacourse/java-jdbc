package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SQLParameters;

import java.sql.SQLException;
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
        final SQLParameters sqlParameters = new SQLParameters()
                .addParameter(user.getAccount())
                .addParameter(user.getPassword())
                .addParameter(user.getEmail());
        try {
            jdbcTemplate.executeQuery(sql, sqlParameters);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(final User user) {
        final String sql = "update users set password = ?, email = ? where id = ?";
        final SQLParameters sqlParameters = new SQLParameters()
                .addParameter(user.getPassword())
                .addParameter(user.getEmail())
                .addParameter(user.getId());
        try {
            jdbcTemplate.executeQuery(sql, sqlParameters);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        try {
            return jdbcTemplate.query(sql, USER_ROW_MAPPER);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final SQLParameters sqlParameters = new SQLParameters();
        sqlParameters.addParameter(id);
        try {
            return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, sqlParameters);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final SQLParameters sqlParameters = new SQLParameters();
        sqlParameters.addParameter(account);
        try {
            return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, sqlParameters);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
