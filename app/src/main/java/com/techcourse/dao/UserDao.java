package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.exception.ResultSetMappingException;

import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> ROW_MAPPER = rs -> {
        try {
            return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"));
        } catch (SQLException e) {
            throw new ResultSetMappingException();
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, email = ?, password = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getEmail(), user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
    }

    public void deleteAll() {
        final var sql = "delete from users";

        jdbcTemplate.update(sql);
    }
}
