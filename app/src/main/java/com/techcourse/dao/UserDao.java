package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private final DataSource dataSource;
    private final RowMapper<User> userMapper = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4));

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.executeUpdate();
            }
        };
        jdbcTemplate.update(sql);
    }


    public void update(User user) {
        final String sql = "update users set password=? where id =?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, user.getPassword());
                pstmt.setLong(2, user.getId());
                pstmt.executeUpdate();
            }
        };
        jdbcTemplate.update(sql);
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {

            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
            }
        };
        return jdbcTemplate.query(sql, userMapper);
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, id);
            }

        };
        return jdbcTemplate.queryForObject(sql, userMapper);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource) {
            @Override
            public void setValues(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, account);
            }
        };
        return jdbcTemplate.queryForObject(sql, userMapper);
    }
}
