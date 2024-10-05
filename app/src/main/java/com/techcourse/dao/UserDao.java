package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ObjectMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final ObjectMapper<User> USER_OBJECT_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        jdbcTemplate.execute("insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.execute("update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.getResults("select id, account, password, email from users",
                USER_OBJECT_MAPPER);
    }

    public User findById(final Long id) {
        return jdbcTemplate.getResult("select id, account, password, email from users where id = ?",
                USER_OBJECT_MAPPER,
                id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.getResult("select id, account, password, email from users where account = ?",
                USER_OBJECT_MAPPER,
                account);
    }
}
