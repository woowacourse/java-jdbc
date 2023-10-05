package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> ROW_MAPPER = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        final long userId = user.getId();
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail(), userId);
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";

        return jdbcTemplate.queryForList(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
    }
}
