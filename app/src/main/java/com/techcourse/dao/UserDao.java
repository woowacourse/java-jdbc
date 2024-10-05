package com.techcourse.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.SqlParameterSource;
import com.interface21.jdbc.core.mapper.RowMapper;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String baseQuery = "insert into users (account, password, email) values (:account, :password, :email)";
        final SqlParameterSource sqlParameterSource = new SqlParameterSource(user);
        jdbcTemplate.insert(baseQuery, sqlParameterSource);
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final String baseQuery = "select id, account, password, email from users where id = :id";
        final Map<String, Object> queryParameters = Map.of("id", id);
        final RowMapper<User> rowMapper = new RowMapper<>(User.class);

        return jdbcTemplate.queryForObject(baseQuery, queryParameters, rowMapper);
    }

    public User findByAccount(final String account) {
        final String baseQuery = "select id, account, password, email from users where account = :account";
        final Map<String, Object> queryParameters = Map.of("account", account);
        final RowMapper<User> rowMapper = new RowMapper<>(User.class);

        return jdbcTemplate.queryForObject(baseQuery, queryParameters, rowMapper);
    }
}
