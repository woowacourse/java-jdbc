package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
//    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (resultSet) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

//    public UserDao(final DataSource dataSource) {
//        this.jdbcTemplate = new JdbcTemplate(dataSource);
//    }
//
//    public UserDao(final JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }

    public void insert(final User user) {
        InsertJdbcTemplate jdbcTemplate = new InsertJdbcTemplate(dataSource);

        jdbcTemplate.insert(user);
    }

    public void update(final User user) {
        UpdateJdbcTemplate jdbcTemplate = new UpdateJdbcTemplate(dataSource);

        jdbcTemplate.update(user);
    }

//
//    public List<User> findAll() {
//        final var sql = "select id, account, password, email from users";
//        return jdbcTemplate.query(sql, userRowMapper);
//    }
//
//    public User findById(final Long id) {
//        final var sql = "select id, account, password, email from users where id = ?";
//
//        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
//    }
//
//    public User findByAccount(final String account) {
//        final var sql = "select id, account, password, email from users where account = ?";
//
//        return jdbcTemplate.queryForObject(sql, userRowMapper, account);
//    }
}
