package com.techcourse.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> new User(
        resultSet.getLong(1),
        resultSet.getString(2),
        resultSet.getString(3),
        resultSet.getString(4));

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.info(sql);
        jdbcTemplate.write(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        log.info(sql);
        jdbcTemplate.write(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(Connection connection, final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.write(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        log.info(sql);
        return jdbcTemplate.readAll(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        log.info(sql);
        final Optional<User> user = jdbcTemplate.read(sql, userRowMapper, id);
        return user.orElseThrow(() -> new IllegalArgumentException("User가 존재하지 않습니다. id = " + id));
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        log.info(sql);
        final Optional<User> user = jdbcTemplate.read(sql, userRowMapper, account);
        return user.orElseThrow(() -> new IllegalArgumentException("User가 존재하지 않습니다. account = " + account));
    }
}
