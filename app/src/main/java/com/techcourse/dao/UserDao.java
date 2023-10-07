package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> userRowMapper = (resultSet, rowNum) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void insert(final Connection connection, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(connection,
                sql,
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password =?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set account = ?, password =?, email = ? where id = ?";
        jdbcTemplate.update(connection,
                sql,
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public List<User> findAll(final Connection connection) {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(connection,
                sql,
                userRowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id)
                .orElseThrow(() -> new NoSuchElementException("id에 해당하는 user가 없습니다."));
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(connection,
                        sql,
                        userRowMapper,
                        id)
                .orElseThrow(() -> new NoSuchElementException("id에 해당하는 user가 없습니다."));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, account)
                .orElseThrow(() -> new NoSuchElementException("account에 해당하는 user가 없습니다."));
    }

    public User findByAccount(final Connection connection, final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(connection,
                        sql,
                        userRowMapper, account)
                .orElseThrow(() -> new NoSuchElementException("account에 해당하는 user가 없습니다."));
    }
}
