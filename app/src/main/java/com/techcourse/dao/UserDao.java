package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "update users set id = ?, account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql,
                user.getId(),
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rs -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)));
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, rs -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)), id);
    }

    public User findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        ), account);
    }
}
