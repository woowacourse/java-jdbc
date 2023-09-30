package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = (rs, rowNum) ->
        new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        String account = user.getAccount();
        String password = user.getPassword();
        String email = user.getEmail();

        jdbcTemplate.update(sql, account, password, email);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();
        final long id = user.getId();

        jdbcTemplate.update(sql, account, password, email, id);
    }

    public List<User> findAll() {
        final var sql = "select * from users";

        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, rowMapper, account);
    }
}
