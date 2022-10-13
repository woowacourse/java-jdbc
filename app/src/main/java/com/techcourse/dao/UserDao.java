package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.support.UserRowMapper;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = new UserRowMapper();

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.queryForList(sql, userRowMapper);
    }

    public User findById(final Long id) {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public User findByAccount(final String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account = ? LIMIT 1";
        return jdbcTemplate.queryForObject(sql, userRowMapper, account);
    }
}
