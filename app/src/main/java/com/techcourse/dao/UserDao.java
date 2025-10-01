package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        jdbcTemplate.update(
                "UPDATE users SET (account, password, email) = (?, ?, ?) WHERE id=?",
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.queryAll(
                "SELECT id, account, password, email FROM users",
                getUserRowMapper()
        );
    }

    public User findById(final Long id) {
        return jdbcTemplate.query(
                "select id, account, password, email from users where id = ?",
                getUserRowMapper(),
                id
        );
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.query(
                "SELECT id, account, password, email FROM users WHERE account=?",
                getUserRowMapper(),
                account
        );
    }

    private RowMapper<User> getUserRowMapper() {
        return (resultSet) -> new User(
                resultSet.getLong(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4)
        );
    }
}
