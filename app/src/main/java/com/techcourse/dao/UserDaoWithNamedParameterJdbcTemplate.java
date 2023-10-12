package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDaoWithNamedParameterJdbcTemplate implements UserRepository {
    public static final String ACCOUNT = "account";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";
    public static final String ID = "id";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserDaoWithNamedParameterJdbcTemplate(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (:account, :password, :email)";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(ACCOUNT, user.getAccount());
        parameters.put(PASSWORD, user.getPassword());
        parameters.put(EMAIL, user.getEmail());
        jdbcTemplate.update(sql, parameters);
    }

    @Override
    public void update(final User user) {
        final String sql = "update users set (account, password, email) = (:account, :password, :email) where id = :id";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(ACCOUNT, user.getAccount());
        parameters.put(PASSWORD, user.getPassword());
        parameters.put(EMAIL, user.getEmail());
        parameters.put(ID, user.getId());
        jdbcTemplate.update(sql, parameters);
    }

    @Override
    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, getUserRowMapper(), Map.of());
    }

    @Override
    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = :id";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(ID, id);
        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), parameters);
    }

    @Override
    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = :account";
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(ACCOUNT, account);
        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), parameters);
    }

    private static RowMapper<User> getUserRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getLong(ID),
                rs.getString(ACCOUNT),
                rs.getString(PASSWORD),
                rs.getString(EMAIL));
    }
}
