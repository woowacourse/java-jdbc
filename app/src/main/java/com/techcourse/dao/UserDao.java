package com.techcourse.dao;

import com.interface21.jdbc.RowMapper;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
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
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "update users set account = ?, password = ?, email = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final Connection conn, final User user) {
        String sql = "update users set account = ?, password = ?, email = ?";

        jdbcTemplate.update(conn, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        log.debug("query : {}", sql);

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findById(final Connection conn, final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(conn, sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
