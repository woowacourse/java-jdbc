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

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return (List<User>) jdbcTemplate.query(sql, makeRowMapper());
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.queryObject(sql, makeRowMapper(), id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.queryObject(sql, makeRowMapper(), account);
    }

    private RowMapper<User> makeRowMapper() {
        return rs -> {
            long id = rs.getLong("id");
            String account = rs.getString("account");
            String password = rs.getString("password");
            String email = rs.getString("email");
            return new User(id, account, password, email);
        };
    }
}
