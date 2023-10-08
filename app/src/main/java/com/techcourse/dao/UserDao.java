package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final RowMapper<User> userRowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Connection connection, final User user) throws SQLException {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        updateQuery(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());

        log.debug("query : {}", sql);
    }

    private void updateQuery(final Connection connection, final String sql, final Object... objects) throws SQLException {
        final int updatedRows = jdbcTemplate.update(connection, sql, objects);
        if (updatedRows < 1) {
            throw new RuntimeException("저장된 데이터가 없습니다.");
        }
    }

    public void update(final Connection connection, final User user) throws SQLException {
        final var sql = "update users set (account, password, email) = (?, ?, ?)";
        updateQuery(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());

        log.debug("query : {}", sql);
    }

    public List<User> findAll(final Connection connection) throws SQLException {
        final var sql = "select id, account, password, email from users";
        final List<User> users = jdbcTemplate.query(connection, sql, userRowMapper);

        log.debug("query : {}", sql);

        return users;
    }

    public User findById(final Connection connection, final Long id) throws SQLException {
        final var sql = "select id, account, password, email from users where id = ?";
        final User user = jdbcTemplate.queryForObject(connection, sql, userRowMapper, id)
                                      .orElseThrow(() -> new RuntimeException("찾는 사용자가 존재하지 않습니다."));

        log.debug("query : {}", sql);

        return user;
    }

    public User findByAccount(final Connection connection, final String account) throws SQLException {
        final var sql = "select id, account, password, email from users where account = ?";
        final User user = jdbcTemplate.queryForObject(connection, sql, userRowMapper, account)
                                      .orElseThrow(() -> new RuntimeException("찾는 사용자가 존재하지 않습니다."));

        log.debug("query : {}", sql);

        return user;
    }
}
