package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;

public class UserDao {

    public static final RowMapper<User> USER_ROW_MAPPER = (resultSet) -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4));
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            log.debug("query : {}", sql);
            return ps;
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setLong(4, user.getId());
            log.debug("query : {}", sql);
            return ps;
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            log.debug("query : {}", sql);
            return ps;
        }, USER_ROW_MAPPER);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, account);
            log.debug("query : {}", sql);
            return ps;
        }, USER_ROW_MAPPER);
    }
}
