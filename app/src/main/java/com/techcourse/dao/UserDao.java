package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log(sql);

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        log(sql);

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        log(sql);

        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        log(sql);

        return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        log(sql);

        return jdbcTemplate.queryForObject(sql, this::mapRowToUser, account);
    }

    private void log(String sql) {
        log.debug("query: {}", sql);
    }

    private User mapRowToUser(ResultSet rs) {
        try {
            return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );
        } catch (SQLException e) {
            throw new IllegalArgumentException();
        }
    }
}
