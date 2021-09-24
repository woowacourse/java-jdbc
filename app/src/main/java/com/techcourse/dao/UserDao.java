package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> mapper = (rs, rowNum) -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        try {
            this.jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        try {
            this.jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        try {
            return this.jdbcTemplate.query(sql, mapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        try {
            return this.jdbcTemplate.queryForObject(sql, mapper, id);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        try {
            return this.jdbcTemplate.queryForObject(sql, mapper, account);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
