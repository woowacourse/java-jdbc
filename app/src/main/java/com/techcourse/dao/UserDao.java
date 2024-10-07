package com.techcourse.dao;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.interface21.jdbc.core.ArgumentPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserDao {

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
        PreparedStatementSetter preparedStatementSetter =
                new ArgumentPreparedStatementSetter(user.getAccount(), user.getPassword(), user.getEmail());
        jdbcTemplate.update(sql, preparedStatementSetter);
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        PreparedStatementSetter preparedStatementSetter =
                new ArgumentPreparedStatementSetter(user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        jdbcTemplate.update(sql, preparedStatementSetter);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.readAll(sql, userRowMapper, new ArgumentPreparedStatementSetter());
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        final Optional<User> user = jdbcTemplate.query(sql, userRowMapper, new ArgumentPreparedStatementSetter(id));
        return user.orElseThrow(() -> new IllegalArgumentException("User가 존재하지 않습니다. id = " + id));
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        final Optional<User> user = jdbcTemplate.query(sql, userRowMapper, new ArgumentPreparedStatementSetter(account));
        return user.orElseThrow(() -> new IllegalArgumentException("User가 존재하지 않습니다. account = " + account));
    }
}
