package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.repository.UserRepository;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDaoWithJdbcTemplate implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoWithJdbcTemplate(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final Connection connection, final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection connection, final User user) {
        final String sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll(final Connection connection) {
        final String sql = "select * from users";
        return jdbcTemplate.query(connection, sql, getUserRowMapper());
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final Optional<User> user = jdbcTemplate.queryForObject(connection, sql, getUserRowMapper(), id);
        if (user.isEmpty()) {
            throw new RuntimeException("유저 없음!");
        }
        return user.get();
    }

    public User findByAccount(final Connection connection, final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final Optional<User> user = jdbcTemplate.queryForObject(connection, sql, getUserRowMapper(), account);
        if (user.isEmpty()) {
            throw new RuntimeException("유저 없음!");
        }
        return user.get();
    }

    private static RowMapper<User> getUserRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }
}
