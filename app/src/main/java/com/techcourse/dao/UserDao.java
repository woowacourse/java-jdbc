package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(
                sql,
                getPreparedStatementSetter(
                        user.getAccount(),
                        user.getPassword(),
                        user.getEmail()
                ));
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(
                sql,
                getPreparedStatementSetter(
                        user.getAccount(),
                        user.getPassword(),
                        user.getEmail(),
                        user.getId()
                ));
    }

    public void update(final Connection connection, final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(
                connection,
                sql,
                getPreparedStatementSetter(
                        user.getAccount(),
                        user.getPassword(),
                        user.getEmail(),
                        user.getId()
                ));
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, getPreparedStatementSetter(), getRowMapper());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, getPreparedStatementSetter(id), getRowMapper());
    }

    public User findById(final Connection connection, final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(connection, sql, getPreparedStatementSetter(id), getRowMapper());
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, getPreparedStatementSetter(account), getRowMapper());
    }

    private static PreparedStatementSetter getPreparedStatementSetter(Object... params) {
        return (pstmt) -> {
            if (params == null) {
                return;
            }
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        };
    }

    private static RowMapper<User> getRowMapper() {
        return (rs) -> {
            final long id = rs.getLong("id");
            final String account = rs.getString("account");
            final String password = rs.getString("password");
            final String email = rs.getString("email");
            return new User(id, account, password, email);
        };
    }
}
