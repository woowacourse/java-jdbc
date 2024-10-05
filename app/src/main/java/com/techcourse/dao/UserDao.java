package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
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
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET password = ? WHERE id = ?";
        jdbcTemplate.executeUpdate(sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.fetchResults(sql, this::userResultMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.fetchResult(sql, this::userResultMapper, id);
    }

    public User findByAccount(final String account) {
        final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.fetchResult(sql, this::userResultMapper, account);
    }

    private User userResultMapper(ResultSet resultSet) {
        try {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException();
        }
    }
}
