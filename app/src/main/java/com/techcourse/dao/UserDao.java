package com.techcourse.dao;

import com.techcourse.dao.exception.UserFoundException;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
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
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(final Connection connection, final User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, rowMapper());
    }

    public User findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper(), id);
        } catch (DataAccessException e) {
            throw new UserFoundException("유저가 없거나 2명 이상 입니다.", e);
        }
    }

    public User findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper(), account);
        } catch (DataAccessException e) {
            throw new UserFoundException("유저가 없거나 2명 이상 입니다.", e);
        }
    }

    public RowMapper<User> rowMapper() {
        return (resultSet -> {
            try {
                long id = resultSet.getLong("id");
                String account = resultSet.getString("account");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                return new User(id, account, password, email);
            } catch (SQLException e) {
                throw new IllegalArgumentException("RowMapper exception !");
            }
        });
    }
}
