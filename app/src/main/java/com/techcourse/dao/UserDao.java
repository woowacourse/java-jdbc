package com.techcourse.dao;

import java.util.List;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> ROW_MAPPER =
            rs -> new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        final var account = user.getAccount();
        final var password = user.getPassword();
        final var email = user.getEmail();

        jdbcTemplate.update(sql, account, password, email);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        final var account = user.getAccount();
        final var password = user.getPassword();
        final var email = user.getEmail();
        final var id = user.getId();

        jdbcTemplate.update(sql, account, password, email, id);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql,
                pstmt -> pstmt.setLong(1, id),
                ROW_MAPPER
        );
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql,
                pstmt -> pstmt.setString(1, account),
                ROW_MAPPER
        );
    }
}
