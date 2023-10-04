package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private final RowMapper<User> userRowMapper =
            rs -> new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());

        log.info("insert(): {}", user);
    }

    public void update(final User user) {
        final String sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());

        log.info("update(): {}", user);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        final List<User> findUsers = jdbcTemplate.query(sql, userRowMapper);

        log.info("findAll(): size = {}", findUsers.size());

        return findUsers;
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        final User findUser = jdbcTemplate.queryForObject(sql, userRowMapper, id);

        log.info("findById(): {}", findUser);

        return findUser;
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        final User findUser = jdbcTemplate.queryForObject(sql, userRowMapper, account);

        log.info("findByAccount(): {}", findUser);

        return findUser;
    }
}
