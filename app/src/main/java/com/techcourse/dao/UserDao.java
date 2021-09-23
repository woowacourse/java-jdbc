package com.techcourse.dao;

import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        PreparedStatementSetter pss = pstmt -> {
        };
        RowMapper<User> rowMapper = this::mapToUser;

        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource);
        return selectJdbcTemplate.query(sql, rowMapper, pss);
    }

    public User findById(Long id) {
        final String sql = "select * from users where id = ?";
        PreparedStatementSetter pss = pstmt -> pstmt.setLong(1, id);
        RowMapper<User> rowMapper = this::mapToUser;

        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource);
        return selectJdbcTemplate.queryForObject(sql, rowMapper, pss);
    }

    public User findByAccount(String account) {
        final String sql = "select * from users where account = ?";

        PreparedStatementSetter pss = pstmt -> pstmt.setString(1, account);
        RowMapper<User> rowMapper = this::mapToUser;

        JdbcTemplate selectJdbcTemplate = new JdbcTemplate(dataSource);
        return selectJdbcTemplate.queryForObject(sql, rowMapper, pss);
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

        PreparedStatementSetter pss = pstmt -> {
        };

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(sql, pss);
    }
}
