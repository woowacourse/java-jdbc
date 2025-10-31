package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter pss = ps -> {
            ps.setString(1, user.getAccount());
            ps.setObject(2, user.getPassword());
            ps.setString(3, user.getEmail());
        };

        jdbcTemplate.update(sql, pss);
    }

    public void update(final User user) {
        final String sql = "update users set password = ?, email = ? where account = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getEmail(), user.getAccount());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.queryForList(sql, userRowMapper);
    }

    public Optional<User> findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public Optional<User> findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, userRowMapper, account);
    }

    public Optional<User> findByAccountWithPss(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        PreparedStatementSetter pss = ps -> ps.setString(1, account);

        return jdbcTemplate.queryForObject(sql, userRowMapper, pss);
    }
}
