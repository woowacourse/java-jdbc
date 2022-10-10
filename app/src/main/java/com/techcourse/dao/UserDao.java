package com.techcourse.dao;

import com.techcourse.domain.User;
import java.text.MessageFormat;
import java.util.List;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.debug(sql);
        int rowCount = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());

        if (rowCount != 1) {
            throw new DataAccessException(MessageFormat.format("wrong query : {0}", sql));
        }
    }

    public void update(final User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        log.debug(sql);
        int rowCount = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());

        if (rowCount != 1) {
            throw new DataAccessException(MessageFormat.format("wrong query : {0}", sql));
        }
    }

    public List<User> findAll() {
        String sql = "select * from users";
        log.debug(sql);
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        String sql = "select * from users where id = ?";
        log.debug(sql);
        return jdbcTemplate.query(sql, ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        String sql = "select * from users where account = ?";
        log.debug(sql);
        return jdbcTemplate.query(sql, ROW_MAPPER, account);
    }
}
