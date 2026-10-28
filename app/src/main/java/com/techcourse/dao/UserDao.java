package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final var affectedRows = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        log.debug("User inserted, affectedRows={}", affectedRows);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final var affectedRows = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        log.debug("User updated, userId={}, affectedRows={}", user.getId(), affectedRows);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        final var users = jdbcTemplate.query(sql, USER_ROW_MAPPER);
        log.debug("Users found, count={}", users.size());
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final var user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
        log.debug("User lookup by id, userId={}, found={}", id, user != null);
        return user;
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final var user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
        log.debug("User lookup by account, found={}", user != null);
        return user;
    }
}
