package com.techcourse.dao;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import com.interface21.jdbc.core.ArgumentPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserDao {
    private static final RowMapper<User> ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var query = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(query, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var query = "update users set account = ?, password = ?, email = ?";

        jdbcTemplate.update(query, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection conn, final User user) {
        final var query = "update users set account = ?, password = ?, email = ?";

        jdbcTemplate.update(conn, query, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        final var query = "select id, account, password, email from users";

        return jdbcTemplate.query(query, ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var query = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(query, ROW_MAPPER, id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 갖는 유저가 없습니다."));
    }

    public User findByAccount(final String account) {
        final var query = "select * from users where account = ?";

        return jdbcTemplate.queryForObject(query, ROW_MAPPER, account)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 account를 갖는 유저가 없습니다."));
    }
}
