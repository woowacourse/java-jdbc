package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.repository.UserMapper;
import nextstep.jdbc.JdbcMapper;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final JdbcMapper<User> userMapper = new UserMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) throws SQLException {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        this.jdbcTemplate.executeQuery(sql, userMapper, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) throws SQLException {
        String sql = "update users set password = ? where account = ?";
        this.jdbcTemplate.executeQuery(sql, userMapper, user.getPassword(), user.getAccount());

    }

    public List<User> findAll() throws SQLException {
        String sql = "select id, account, password, email from users";
        return this.jdbcTemplate.executeQuery(sql, userMapper);
    }

    public User findById(final Long id) throws SQLException {
        String sql = "select id, account, password, email from users where id = ?";
        List<User> users = this.jdbcTemplate.executeQuery(sql, userMapper, id);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    public User findByAccount(final String account) throws SQLException {
        String sql = "select id, account, password, email from users where account = ?";
        List<User> users = this.jdbcTemplate.executeQuery(sql, userMapper, account);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }
}
