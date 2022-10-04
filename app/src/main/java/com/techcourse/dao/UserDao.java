package com.techcourse.dao;

import com.techcourse.domain.User;
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

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) throws SQLException {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        List<Object> params = new ArrayList<>();
        params.add(user.getAccount());
        params.add(user.getPassword());
        params.add(user.getEmail());
        this.jdbcTemplate.executeQuery(sql, params);
    }

    public void update(final User user) throws SQLException {
        String sql = "update users set password = ? where account = ?";
        List<Object> params = new ArrayList<>();
        params.add(user.getPassword());
        params.add(user.getAccount());
        this.jdbcTemplate.executeQuery(sql, params);

    }

    public List<User> findAll() throws SQLException {
        String sql = "select id, account, password, email from users";
        List<Object> params = new ArrayList<>();
        List<List<Object>> results = this.jdbcTemplate.executeQuery(sql, params);
        List<User> users = new ArrayList<>();
        for (List<Object> each : results) {
            users.add(new User((Long) each.get(0), (String) each.get(1), (String) each.get(2), (String) each.get(3)));
        }
        return users;
    }

    public User findById(final Long id) throws SQLException {
        String sql = "select id, account, password, email from users where id = ?";
        List<Object> params = new ArrayList<>();
        params.add(id);
        List<List<Object>> results = this.jdbcTemplate.executeQuery(sql, params);
        return new User(
                (Long) results.get(0).get(0),
                (String) results.get(0).get(1),
                (String) results.get(0).get(2),
                (String) results.get(0).get(3)
        );
    }

    public User findByAccount(final String account) throws SQLException {
        String sql = "select id, account, password, email from users where account = ?";
        List<Object> params = new ArrayList<>();
        params.add(account);
        List<List<Object>> results = this.jdbcTemplate.executeQuery(sql, params);
        return new User(
                (Long) results.get(0).get(0),
                (String) results.get(0).get(1),
                (String) results.get(0).get(2),
                (String) results.get(0).get(3)
        );
    }
}
