package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"), rs.getString("account"), rs.getString("password"), rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setLong(4, user.getId());
        });

    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id=?";
        final PreparedStatementSetter pss = ps -> ps.setLong(1, id);
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, pss)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 id = " + id));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account=?";
        final PreparedStatementSetter pss = ps -> ps.setString(1, account);
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, pss)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 account = " + account));
    }
}
