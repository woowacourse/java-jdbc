package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> rowMapper = rs -> {
        long id = rs.getLong("id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");
        return new User(id, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Connection connection, final User user) {
        jdbcTemplate.execute(connection, "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection connection, final User user) {
        jdbcTemplate.execute(connection, "update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.query("select id, account, password, email from users", rowMapper);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject("select id, account, password, email from users where id = ?",
                rowMapper, id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryForObject("select id, account, password, email from users where account = ?"
                , rowMapper, account);
    }

    public void delete(final Connection connection) {
        jdbcTemplate.execute(connection, "delete from users");
    }
}
