package com.techcourse.dao;

import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import static com.techcourse.config.DataSourceConfig.NO_CONNECTION;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper;

    public UserDao(final DataSource dataSource, final RowMapper<User> rowMapper) {
        this(new JdbcTemplate(dataSource), rowMapper);
    }

    public UserDao(final JdbcTemplate jdbcTemplate, final RowMapper<User> rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(NO_CONNECTION, sql, new Object[]{user.getAccount(), user.getPassword(), user.getEmail()});
    }

    public void update(final User user) {
        final var sql = "update users set password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(NO_CONNECTION, sql, new Object[]{user.getPassword(), user.getEmail(), user.getId()});
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(connection, sql, new Object[]{user.getPassword(), user.getEmail(), user.getId()});
    }

    public List<User> findAll() {
        final var sql = "select * from users";
        return jdbcTemplate.query(NO_CONNECTION, sql, rowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(NO_CONNECTION, sql, new Object[]{id}, rowMapper);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(NO_CONNECTION, sql, new Object[]{account}, rowMapper);
    }
}
