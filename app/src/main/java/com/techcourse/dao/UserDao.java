package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, List.of(user.getAccount(), user.getPassword(), user.getEmail()));
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ?";
        try {
            jdbcTemplate.update(sql, List.of(user.getAccount(), user.getPassword(), user.getEmail()));
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, List.of(id), new UserRowMapper());
    }

    public User findByAccount(final String account) {
        // todo
        return null;
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
