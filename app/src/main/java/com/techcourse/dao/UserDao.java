package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;

public class UserDao {

    private static final RowMapper<User> userRowMapper = (rs) -> new User(
        rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        return jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public int update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        return jdbcTemplate
            .update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.queryForList(sql, userRowMapper);
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, account);
    }

    public int remove(String account) {
        final String sql = "delete from users where account = ?";
        return jdbcTemplate.update(sql, account);
    }
}
