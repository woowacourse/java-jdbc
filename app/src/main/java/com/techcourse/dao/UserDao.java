package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao() {
        this(new JdbcTemplate(DataSourceConfig.getInstance()));
    }

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select * from users";
        return jdbcTemplate.queryForObject(sql, User.class);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ? limit 1";
        List<User> users = jdbcTemplate.queryForObject(sql, User.class, id);

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        List<User> users = jdbcTemplate.queryForObject(sql, User.class, account);

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0);
    }
}
