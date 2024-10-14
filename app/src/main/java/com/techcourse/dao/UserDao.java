package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

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

    public void update(User user) {
        Connection connection = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        logSql(sql);
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "SELECT id, account, password, email FROM users";
        logSql(sql);
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(Long id) {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        logSql(sql);
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    public User findByAccount(String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        logSql(sql);
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, account);
    }

    private void logSql(String sql) {
        log.debug("query : {}", sql);
    }
}
