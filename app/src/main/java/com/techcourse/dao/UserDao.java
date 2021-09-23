package com.techcourse.dao;

import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userMapper = rs -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, userMapper);
    }

    public User findById(Long id) {
        final String sql = "select * from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userMapper, id);
    }

    public User findByAccount(String account) {
        final String sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userMapper, account);
    }

    public void clear() {
        String sql = "drop table users";
        jdbcTemplate.execute(sql);
    }
}
