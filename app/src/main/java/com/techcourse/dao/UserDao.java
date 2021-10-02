package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public int update(User user) {
        final String sql = "update users SET password = ? WHERE account = ?";
        return jdbcTemplate.update(sql, user.getPassword(), user.getAccount());
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, userRowMapper());
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper(), id);
    }

    public User findByAccount(String account) {
        final String sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper(), account);
    }

    private RowMapper<User> userRowMapper() {
        return rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
