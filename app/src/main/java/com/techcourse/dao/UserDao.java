package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.exception.UserNotFoundException;
import com.techcourse.repository.UserRowMapper;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_MAPPER = new UserRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(final Connection connection, final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email from users";

        return jdbcTemplate.query(sql, USER_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, USER_MAPPER, id);
        } catch (Exception e) {
            throw new UserNotFoundException("user not found (id: " + id);
        }
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email from users where account = ?";

        try {
            return jdbcTemplate.queryForObject(sql, USER_MAPPER, account);
        } catch (Exception e) {
            throw new UserNotFoundException("user not found (account: " + account + ")");
        }
    }
}
