package com.techcourse.dao;

import com.interface21.jdbc.core.EntityMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.repository.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final EntityMapper<User> USER_MAPPER = new UserMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final List<Object> parameters =
            List.of(user.getAccount(), user.getPassword(), user.getEmail());

        jdbcTemplate.executeUpdate(sql, parameters);
    }

    public void update(final User user) {
        // todo
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final List<Object> parameters =
            List.of(user.getAccount(), user.getPassword(), user.getEmail(), user.getId());

        jdbcTemplate.executeUpdate(sql, parameters);
    }

    public List<User> findAll() {
        // todo
        final String sql = "SELECT id, account, password, email from users";
        final List<Object> parameters = List.of();

        return jdbcTemplate.executeSelectAll(sql, USER_MAPPER, parameters);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        final List<Object> parameters = List.of(id);

        return jdbcTemplate.executeSelect(sql, USER_MAPPER, parameters)
            .orElseThrow(() -> new IllegalArgumentException("user not found (id: " + id));
    }

    public User findByAccount(final String account) {
        // todo
        final String sql = "SELECT id, account, password, email from users where account = ?";
        final List<Object> parameters = List.of(account);

        return jdbcTemplate.executeSelect(sql, USER_MAPPER, parameters)
            .orElseThrow(() -> new IllegalArgumentException("user not found (account: " + account));
    }
}
