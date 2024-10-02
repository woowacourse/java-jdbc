package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void insert(User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        logSql(sql);
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        logSql(sql);
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        logSql(sql);
        return jdbcTemplate.query(sql, this::rowMapper);
    }

    public User findById(Long id) {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        logSql(sql);
        return jdbcTemplate.queryForObject(sql, this::rowMapper, id);
    }

    public User findByAccount(String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        logSql(sql);
        return jdbcTemplate.queryForObject(sql, this::rowMapper, account);
    }

    private void logSql(String sql) {
        log.debug("query : {}", sql);
    }

    private User rowMapper(ResultSet rs) {
        try {
            return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );
        } catch (SQLException e) {
            throw new IllegalStateException("쿼리 실행 결과가 User 형식과 일치하지 않습니다.", e);
        }
    }
}
