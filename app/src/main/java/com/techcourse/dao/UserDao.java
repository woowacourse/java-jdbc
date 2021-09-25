package com.techcourse.dao;

import java.util.List;
import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        jdbcTemplate.query(sql, userMapper());
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userMapper(), id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
       return jdbcTemplate.queryForObject(sql, userMapper(), account);
    }

    private RowMapper<User> userMapper() {
        return (rs, rm) -> new User(
                rs.getString("name"),
                rs.getString("name"),
                rs.getString("description")
        );
    }
}
