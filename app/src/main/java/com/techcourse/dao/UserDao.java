package com.techcourse.dao;

import com.techcourse.domain.User;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Long id = jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());
        user.setId(id);
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.finds(User.class, sql)
                .stream()
                .map(User.class::cast)
                .collect(Collectors.toList());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return (User)jdbcTemplate.find(User.class, sql, id);
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return (User)jdbcTemplate.find(User.class, sql, account);
    }

    public void deleteAll() {
        final String sql = "DELETE FROM users";
        jdbcTemplate.deleteAll(sql);
    }
}
