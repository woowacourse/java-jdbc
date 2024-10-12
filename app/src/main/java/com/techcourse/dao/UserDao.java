package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> userRowMapper = (rs) -> new User(
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

    public void insert(Connection conn, User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(conn, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void insertWithPss(Connection conn, User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";

        PreparedStatementSetter pss = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };

        jdbcTemplate.update(conn, sql, pss);
    }

    public void update(Connection conn, User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(conn, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll(Connection conn) {
        String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.query(conn, sql, userRowMapper);
    }

    public List<User> findAllByEmailWithPss(Connection conn, String email) {
        String sql = "SELECT id, account, password, email FROM users WHERE email = ?";

        PreparedStatementSetter pss = pstmt -> {
            pstmt.setString(1, email);
        };

        return jdbcTemplate.query(conn, sql, pss, userRowMapper);
    }

    public User findById(Connection conn, Long id) {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(conn, sql, userRowMapper, id);
    }

    public User findByAccount(Connection conn, String account) {
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        return jdbcTemplate.queryForObject(conn, sql, userRowMapper, account);
    }
}
