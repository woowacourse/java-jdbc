package com.techcourse.dao;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.SqlParameterSource;
import com.interface21.jdbc.core.mapper.RowMapper;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final String baseQuery = "INSERT INTO users (account, password, email) VALUES (:account, :password, :email)";
        final SqlParameterSource sqlParameterSource = new SqlParameterSource(user);
        jdbcTemplate.insert(baseQuery, sqlParameterSource);
    }

    private RowMapper<User> rowMapper() {
        return (rs) -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }

    public void update(final User user) {
        final String baseQuery = "UPDATE users SET account = :account, password = :password, email = :email WHERE id = :id";
        final Map<String, Object> parameters = Map.of(
                "account", user.getAccount(),
                "password", user.getPassword(),
                "email", user.getEmail(),
                "id", user.getId()
        );
        jdbcTemplate.update(baseQuery, parameters);
    }

    public void update(final Connection connection, final User user) {
        final String baseQuery = "UPDATE users SET account = :account, password = :password, email = :email WHERE id = :id";
        final Map<String, Object> parameters = Map.of(
                "account", user.getAccount(),
                "password", user.getPassword(),
                "email", user.getEmail(),
                "id", user.getId()
        );
        jdbcTemplate.update(connection, baseQuery, parameters);
    }

    public List<User> findAll() {
        final String baseQuery = "SELECT * FROM users";
        final RowMapper<User> rowMapper = rowMapper();
        return jdbcTemplate.query(baseQuery, Collections.emptyMap(), rowMapper);
    }

    public User findById(final Long id) {
        final String baseQuery = "SELECT id, account, password, email FROM users WHERE id = :id";
        final Map<String, Object> queryParameters = Map.of("id", id);
        final RowMapper<User> rowMapper = rowMapper();

        return jdbcTemplate.queryForObject(baseQuery, queryParameters, rowMapper);
    }

    public User findByAccount(final String account) {
        final String baseQuery = "SELECT id, account, password, email FROM users WHERE account = :account";
        final Map<String, Object> queryParameters = Map.of("account", account);
        final RowMapper<User> rowMapper = rowMapper();

        return jdbcTemplate.queryForObject(baseQuery, queryParameters, rowMapper);
    }
}
