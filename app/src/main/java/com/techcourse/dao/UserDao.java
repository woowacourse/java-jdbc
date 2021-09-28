package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
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
        final String sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, new UserMapper());
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, new UserMapper(), id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account= ?";
        return jdbcTemplate.queryForObject(sql, new UserMapper(), account);
    }

    public void dropTable(String table) {
        final String sql = "drop table if exists " + table;
        jdbcTemplate.update(sql);
    }
}
