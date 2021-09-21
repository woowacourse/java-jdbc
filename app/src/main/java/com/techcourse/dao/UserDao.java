package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.core.RowMapper;
import nextstep.jdbc.util.DataAccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userRowMapper = rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(Long id) {
        final String sql = "select * from users where id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return Optional.ofNullable(DataAccessUtils.singleResult(users));
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select * from users where account = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, account);
        return Optional.ofNullable(DataAccessUtils.singleResult(users));
    }
}
