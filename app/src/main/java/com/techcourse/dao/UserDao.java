package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

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
        Object[] args = new Object[3];
        args[0] = user.getAccount();
        args[1] = user.getPassword();
        args[2] = user.getEmail();

        jdbcTemplate.update(sql, args);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        Object[] args = new Object[4];
        args[0] = user.getAccount();
        args[1] = user.getPassword();
        args[2] = user.getEmail();
        args[3] = user.getId();

        jdbcTemplate.update(sql, args);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rowMapper());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        Object[] args = new Object[1];
        args[0] = id;

        return jdbcTemplate.queryForObject(sql, args, rowMapper());
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        Object[] args = new Object[1];
        args[0] = account;

        return jdbcTemplate.queryForObject(sql, args, rowMapper());
    }

    private RowMapper<User> rowMapper() {
        return rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
