package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.core.SimpleJdbcTemplate;
import com.techcourse.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleUserDao {

    private static final Logger log = LoggerFactory.getLogger(SimpleUserDao.class);

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final SimpleJdbcTemplate jdbcTemplate;

    public SimpleUserDao(final DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (:account, :password, :email)";
        final Map<String, Object> params = new HashMap<>();
        params.put("account", user.getAccount());
        params.put("password", user.getPassword());
        params.put("email", user.getEmail());
        jdbcTemplate.updateWithParam(sql, params);
    }

    public void update(final User user) {
        final var sql = "update users set account = :account, password = :password, email = :email where id = :id";
        final Map<String, Object> params = new HashMap<>();
        params.put("account", user.getAccount());
        params.put("password", user.getPassword());
        params.put("email", user.getEmail());
        params.put("id", user.getId());
        jdbcTemplate.updateWithParam(sql, params);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = :id";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, Map.of("id", id));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = :account";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, Map.of("account", account));
    }

    public void deleteAll() {
        final var sql = "delete from users";
        jdbcTemplate.update(sql);
    }
}
