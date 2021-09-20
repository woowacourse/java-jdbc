package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.exception.UserNotFoundException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.templates.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        } catch (SQLException sqlException) {
            throw new IllegalStateException(sqlException);
        }
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        try {
            jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        } catch (SQLException sqlException) {
            throw new IllegalStateException(sqlException);
        }
    }

    public List<User> findAll() {
        try {
            final String sql = "select * from users";
            return jdbcTemplate.queryForList(sql, User.class);
        } catch (SQLException sqlException) {
            throw new IllegalStateException(sqlException);
        }
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, User.class, id)
                .orElseThrow(UserNotFoundException::new);
        } catch (SQLException sqlException) {
            throw new IllegalStateException(sqlException);
        }
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        try {
            return jdbcTemplate.queryForObject(sql, User.class, account)
                .orElseThrow(UserNotFoundException::new);
        } catch (SQLException sqlException) {
            throw new IllegalStateException(sqlException);
        }
    }

    public void removeAll() {
        final String sql = "delete from users";
        try {
            jdbcTemplate.update(sql);
        } catch (SQLException sqlException) {
            throw new IllegalStateException(sqlException);
        }
    }
}
