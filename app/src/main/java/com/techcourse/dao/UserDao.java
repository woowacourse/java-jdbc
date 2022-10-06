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
        this.jdbcTemplate.executeQuery(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) throws SQLException {
        String sql = "update users set password = ? where account = ?";
        this.jdbcTemplate.executeQuery(sql, user.getPassword(), user.getAccount());

    }

    public List<User> findAll() throws SQLException {
        String sql = "select id, account, password, email from users";
        List<List<Object>> results = this.jdbcTemplate.executeQuery(sql);
        List<User> users = new ArrayList<>();
        for (List<Object> each : results) {
            users.add(new User((Long) each.get(0), (String) each.get(1), (String) each.get(2), (String) each.get(3)));
        }
        return users;
    }

    public User findById(final Long id) throws SQLException {
        String sql = "select id, account, password, email from users where id = ?";
        List<List<Object>> results = this.jdbcTemplate.executeQuery(sql, id);
        return new User(
                (Long) results.get(0).get(0),
                (String) results.get(0).get(1),
                (String) results.get(0).get(2),
                (String) results.get(0).get(3)
        );
    }

    public User findByAccount(final String account) throws SQLException {
        String sql = "select id, account, password, email from users where account = ?";
        List<List<Object>> results = this.jdbcTemplate.executeQuery(sql, account);
        return new User(
                (Long) results.get(0).get(0),
                (String) results.get(0).get(1),
                (String) results.get(0).get(2),
                (String) results.get(0).get(3)
        );
    }
}
