package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> userRowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        int rowCount = jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
        log.debug("insert 성공한 row 개수 : {}", rowCount);
        return rowCount;
    }

    public int update(final User user) {
        String sql = "update users set account=?, password=?, email=? where id=?";
        int rowCount = jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getPassword(), user.getId());
        log.debug("update 성공한 row 개수 : {}", rowCount);
        return rowCount;
    }

    public List<User> findAll() {
        String sql = "select * from users";
        List<User> result = jdbcTemplate.query(sql, userRowMapper);
        log.debug("select 성공한 row 개수 : {}", result.size());
        return result;
    }

    public User findById(final Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        User result = jdbcTemplate.queryForObject(sql, userRowMapper, id);
        log.debug("select 성공한 row id : {}", result.getId());
        return result;
    }

    public User findByAccount(final String account) {
        String sql = "select id, account, password, email from users where account = ?";
        User result = jdbcTemplate.queryForObject(sql, userRowMapper, account);
        log.debug("select 성공한 row id : {}", result.getId());
        return result;
    }
}
