package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void insert(User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        String account = user.getAccount();
        String password = user.getPassword();
        String email = user.getEmail();

        jdbcTemplate.update(sql, account, password, email);
    }

    public void update(User user) {
        final var sql = "update users set account=?, password=?, email=? where id=?";
        String account = user.getAccount();
        String password = user.getPassword();
        String email = user.getEmail();
        long id = user.getId();

        jdbcTemplate.update(sql, account, password, email, id);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForAll(User.class, sql);
    }

    public User findById(Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.query(User.class, sql, id);
    }

    public User findByAccount(String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.query(User.class, sql, account);
    }
}
