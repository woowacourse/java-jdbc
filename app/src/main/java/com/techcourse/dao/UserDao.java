package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, ps -> {
            ps.setLong(1, id);
        }, rs -> {
            long userId = rs.getLong(1);
            String account = rs.getString(2);
            String password = rs.getString(3);
            String email = rs.getString(4);
            return new User(userId, account, password, email);
        });
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, ps -> {
            ps.setString(1, account);
        }, rs -> {
            long userId = rs.getLong(1);
            String userAccount = rs.getString(2);
            String password = rs.getString(3);
            String email = rs.getString(4);
            return new User(userId, userAccount, password, email);
        });
    }
}
