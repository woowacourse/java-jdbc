package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Connection connection,
                       final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();

        log.debug("sql={}", sql);

        jdbcTemplate.update(connection, sql, account, password, email);
    }

    public void update(final Connection connection,
                       final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();
        final Long id = user.getId();

        log.debug("sql={}", sql);

        jdbcTemplate.update(connection, sql, account, password, email, id);
    }

    public List<User> findAll(final Connection connection) {
        final String sql = "SELECT id, account, password, email FROM users";

        log.debug("sql={}", sql);

        return jdbcTemplate.query(connection, sql, rowMapper);
    }

    public User findById(final Connection connection,
                         final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        log.debug("sql={}", sql);

        Optional<User> user = jdbcTemplate.querySingleRow(connection, sql, rowMapper, id);

        return user.orElseThrow(() -> new IllegalArgumentException("해당 아이디로 조회되는 유저가 없습니다."));
    }

    public User findByAccount(final Connection connection,
                              final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        log.debug("sql={}", sql);

        Optional<User> user = jdbcTemplate.querySingleRow(connection, sql, rowMapper, account);

        return user.orElseThrow(() -> new IllegalArgumentException("해당 계정으로 조회되는 유로가 없습니다."));
    }
}
