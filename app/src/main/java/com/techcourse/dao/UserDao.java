package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        log.debug("query : {}", sql);

        jdbcTemplate.update(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        log.debug("query : {}", sql);

        jdbcTemplate.update(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        log.debug("query : {}", sql);

        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, this::mapRowToUser, account);
    }

    private User mapRowToUser(final ResultSet rs, final int rowNum) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
