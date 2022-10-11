package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.service.InvalidRequestException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), account);
    }

    private static class UserRowMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4));
        }
    }
}
