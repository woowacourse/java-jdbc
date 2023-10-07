package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final ResultSetMapper<User> RESULT_SET_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final Connection connection, final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection connection, final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, RESULT_SET_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, RESULT_SET_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, RESULT_SET_MAPPER, account);
    }
}
