package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class UserDao {

    private static final RowMapper<User> USER_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final InsertJdbcTemplate insertJdbcTemplate;
    private final UpdateJdbcTemplate updateJdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertJdbcTemplate = new InsertJdbcTemplate(jdbcTemplate);
        this.updateJdbcTemplate = new UpdateJdbcTemplate(jdbcTemplate);
    }

    public void insert(final User user) {
        insertJdbcTemplate.insert(user, this);
    }

    public void update(final User user) {
        updateJdbcTemplate.update(user, this);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = :id";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return jdbcTemplate.queryForObject(sql, USER_MAPPER, params);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = :account";
        Map<String, Object> params = new HashMap<>();
        params.put("account", account);
        return jdbcTemplate.queryForObject(sql, USER_MAPPER, params);
    }

    public void deleteAll() {
        final var sql = "delete from users";
        jdbcTemplate.update(sql);
    }
}
