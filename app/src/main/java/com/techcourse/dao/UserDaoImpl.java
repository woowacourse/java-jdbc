package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @Override
    public void update(final User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    @Override
    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, getUserRowMapper());
    }

    @Override
    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), id)
                .orElseThrow(() -> new IllegalArgumentException("id가 " + id + "인 유저를 찾을 수 없습니다."));
    }

    @Override
    public User findByAccount(final String account) {
        final String sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, getUserRowMapper(), account)
                .orElseThrow(() -> new IllegalArgumentException("account가 " + account + "인 유저를 찾을 수 없습니다."));
    }

    private RowMapper<User> getUserRowMapper() {
        return ((resultSet, rowNum) ->
                new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                )
        );
    }

}
