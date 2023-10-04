package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper() {
        return rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }

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

    public void update(final User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";

        return jdbcTemplate.queryForObjects(sql, userRowMapper());
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, userRowMapper(), id);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, userRowMapper(), account);
    }
}
