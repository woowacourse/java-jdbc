package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private static final String UPDATE_SQL = "update users set account = ?, password = ?, email = ? where id = ?";

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final int updatedRows = jdbcTemplate.update(UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        validateUpdated(updatedRows, user);
    }

    public void update(final Connection conn, final User user) {
        final int updatedRows = jdbcTemplate.update(conn, UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        validateUpdated(updatedRows, user);
    }

    private void validateUpdated(final int updatedRows, final User user) {
        if (updatedRows == 0) {
            throw new DataAccessException("id가 " + user.getId() + "인 사용자가 없어 수정하지 못했습니다.");
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id)
                .orElseThrow(() -> new DataAccessException("id가 " + id + "인 사용자가 없습니다."));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account)
                .orElseThrow(() -> new DataAccessException("account가 " + account + "인 사용자가 없습니다."));
    }
}
