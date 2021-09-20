package com.techcourse.dao;

import com.techcourse.config.JdbcTemplateConfig;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import nextstep.jdbc.JdbcException;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private static UserDao userDao;

    private final JdbcTemplate jdbcTemplate;

    private UserDao() {
        this.jdbcTemplate = JdbcTemplateConfig.getInstance();
    }

    public static UserDao getInstance() {
        if (Objects.isNull(userDao)) {
            userDao = new UserDao();
        }
        return userDao;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set password = ? where id = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id));
        } catch (JdbcException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, account));
        } catch (JdbcException e) {
            return Optional.empty();
        }
    }
}
