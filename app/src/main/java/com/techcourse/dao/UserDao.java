package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        long id = rs.getLong("id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");
        return new User(id, account, password, email);
    };

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        try {
            jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void update(final User user) {
        String sql = "update users set account=?, password=?, email=? where id = ?";
        try {
            jdbcTemplate.execute(
                    sql,
                    user.getAccount(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getId()
            );
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        try {
            return jdbcTemplate.query(sql, rowMapper);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public User findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public User findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, account);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
