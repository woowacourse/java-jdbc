package com.techcourse.dao;

import com.techcourse.domain.User;
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

    public void insert(User user, Connection connection) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, connection, user.getAccount(), user.getPassword(), user.getEmail());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void update(User user, Connection connection) {
        String sql = "update users set account = ?, password = ?, email = ?";
        try {
            jdbcTemplate.update(sql, connection, user.getAccount(), user.getPassword(), user.getEmail());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<User> findAll(Connection connection) {
        String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, new UserRowMapper(), connection);
    }

    public User findById(Long id, Connection connection) {
        String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), connection, id);
    }

    public User findByAccount(String account, Connection connection) {
        String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), connection, account);
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
