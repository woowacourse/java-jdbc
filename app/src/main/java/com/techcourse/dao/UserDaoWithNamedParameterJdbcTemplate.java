package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDaoWithNamedParameterJdbcTemplate {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserDaoWithNamedParameterJdbcTemplate(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (:account, :password, :email)";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("account", user.getAccount());
        parameters.put("password", user.getPassword());
        parameters.put("email", user.getEmail());
        jdbcTemplate.update(sql, parameters);
    }

    public void update(final User user) {
        final String sql = "update users set (account, password, email) = (:account, :password, :email) where id = :id";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("account", user.getAccount());
        parameters.put("password", user.getPassword());
        parameters.put("email", user.getEmail());
        parameters.put("id", user.getId());
        jdbcTemplate.update(sql, parameters);
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, getUserRowMapper(), Map.of());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = :id";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), parameters);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = :account";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("account", account);
        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), parameters);
    }

    private static RowMapper<User> getUserRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }
}
