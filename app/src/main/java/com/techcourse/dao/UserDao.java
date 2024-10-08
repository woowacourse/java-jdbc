package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
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
        return jdbcTemplate.queryForList(sql, User.class);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ? limit 1";
        List<User> users = jdbcTemplate.queryForList(sql, User.class, id);

        return getOptionalResult(users);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        List<User> users = jdbcTemplate.queryForList(sql, User.class, account);

        return getOptionalResult(users);
    }

    private Optional<User> getOptionalResult(List<User> users) {
        if (users.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(users.getFirst());
    }
}
