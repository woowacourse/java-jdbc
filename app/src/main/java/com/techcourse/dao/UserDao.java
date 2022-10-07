package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.repository.UserMapper;
import nextstep.jdbc.JdbcMapper;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final JdbcMapper<User> userMapper = new UserMapper();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        this.jdbcTemplate.nonSelectQuery(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "update users set password = ? where account = ?";
        this.jdbcTemplate.nonSelectQuery(sql, user.getPassword(), user.getAccount());

    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return this.jdbcTemplate.selectQuery(sql, userMapper);
    }

    public User findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        List<User> users = this.jdbcTemplate.selectQuery(sql, userMapper, id);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    public User findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        List<User> users = this.jdbcTemplate.selectQuery(sql, userMapper, account);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }
}
