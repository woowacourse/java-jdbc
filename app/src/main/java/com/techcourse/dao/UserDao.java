package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter pss = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(sql, pss);
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        PreparedStatementSetter pss = pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(sql, pss);
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        PreparedStatementSetter pss = pstmt -> {};
        RowMapper rowMapper = rs -> {
            ArrayList<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = mapToUser(rs);
                users.add(user);
            }
            return users;
        };

        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource);
        return (List<User>) selectJdbcTemplate.query(sql, rowMapper, pss);
    }

    public User findById(Long id) {
        final String sql = "select * from users where id = ?";
        PreparedStatementSetter pss = pstmt -> pstmt.setLong(1, id);
        RowMapper rowMapper = rs -> {
            if (!rs.next()) {
                return null;
            }
            return mapToUser(rs);
        };

        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource);
        return (User) selectJdbcTemplate.query(sql, rowMapper, pss);
    }

    public User findByAccount(String account) {
        final String sql = "select * from users where account = ?";

        PreparedStatementSetter pss = pstmt -> pstmt.setString(1, account);
        RowMapper rowMapper = rs -> {
            if (!rs.next()) {
                return null;
            }
            return mapToUser(rs);
        };

        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource);
        return (User) selectJdbcTemplate.query(sql, rowMapper, pss);
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4));
    }

    public void clear() {
        String sql = "drop table users";

        PreparedStatementSetter pss = pstmt -> {};

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(sql, pss);
    }
}
