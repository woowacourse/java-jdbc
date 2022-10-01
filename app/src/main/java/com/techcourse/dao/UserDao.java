package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = rs -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeUpdate(sql, (stmt, s) -> {
            stmt.setString(1, user.getAccount());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            return stmt;
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ?";
        jdbcTemplate.executeUpdate(sql, (stmt, s) -> {
            stmt.setString(1, user.getAccount());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            return stmt;
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, rowMapper);

    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = " + id;
        return jdbcTemplate.queryForObject(sql, rowMapper);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = '" + account + "'";
        return jdbcTemplate.queryForObject(sql, rowMapper);
    }
}
