package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER =
            (rs, rowNum) -> new User(
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
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, new Object[]{
                user.getAccount(),
                user.getPassword(),
                user.getEmail()});
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()});
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, new Object[]{id});
    }

    public User findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, new Object[]{account});
    }
}
