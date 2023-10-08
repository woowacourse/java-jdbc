package com.techcourse.dao;

import java.util.List;
import java.util.Optional;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        log.debug("query : {}", sql);

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ? , email = ? where id = ?";

        log.debug("query : {}", sql);

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public Optional<User> findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(USER_MAPPER, sql, id);
    }

    public Optional<User> findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(USER_MAPPER, sql, account);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(USER_MAPPER, sql);
    }

}
