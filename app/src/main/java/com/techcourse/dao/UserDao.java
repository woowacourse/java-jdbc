package com.techcourse.dao;

import com.interface21.context.stereotype.Component;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.mapper.UserMapper;
import java.sql.Connection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Component
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final String FIND_BY_ID_SQL = "select id, account, password, email from users where id = ?";
    private static final String UPDATE_SQL = "update users set account = ?, password = ?, email = ? where id = ?";

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.queryForUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        return jdbcTemplate.queryForResultList("SELECT * FROM users", UserMapper.USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        Optional<User> user = jdbcTemplate.queryForResult(FIND_BY_ID_SQL, UserMapper.USER_ROW_MAPPER, id);
        return user.orElseThrow(IllegalArgumentException::new);
    }

    public User findById(Connection conn, final Long id) {
        Optional<User> user = jdbcTemplate.queryForResult(conn, FIND_BY_ID_SQL, UserMapper.USER_ROW_MAPPER, id);
        return user.orElseThrow(IllegalArgumentException::new);
    }

    public void update(final User user) {
        jdbcTemplate.queryForUpdate(UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void update(Connection conn, final User user) {
        jdbcTemplate.queryForUpdate(conn, UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        Optional<User> user = jdbcTemplate.queryForResult(sql, UserMapper.USER_ROW_MAPPER, account);
        return user.orElseThrow(IllegalArgumentException::new);
    }
}