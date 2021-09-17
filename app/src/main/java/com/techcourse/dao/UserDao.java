package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set password = ? where id = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql);
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryObject(sql, id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryObject(sql, account);
    }
}
