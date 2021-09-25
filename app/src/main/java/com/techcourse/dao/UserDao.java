package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        log.info("UserDao.insert");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        log.info("UserDao.update");
        final String sql = "update users set password=? where id=?";
        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        log.info("UserDao.findAll");
        final String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userMapper);
    }

    public User findById(Long id) {
        log.info("UserDao.findById");
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userMapper, id);
    }

    public User findByAccount(String account) {
        log.info("UserDao.findByAccount");
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userMapper, account);
    }
}
