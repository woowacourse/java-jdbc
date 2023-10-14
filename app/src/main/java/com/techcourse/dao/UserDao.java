package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final RowMapper<User> userRowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(final User user) throws SQLException {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.debug("query : {}", sql);

        return updateQuery(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    private int updateQuery(final String sql, final Object... objects) throws SQLException {
        final int updatedRows = jdbcTemplate.update(sql, objects);
        if (updatedRows < 1) {
            throw new RuntimeException("저장된 데이터가 없습니다.");
        }
        return updatedRows;
    }

    public int update(final User user) throws SQLException {
        final var sql = "update users set (account, password, email) = (?, ?, ?)";
        log.debug("query : {}", sql);

        return updateQuery(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() throws SQLException {
        final var sql = "select id, account, password, email from users";
        final List<User> users = jdbcTemplate.query(sql, userRowMapper);

        log.debug("query : {}", sql);

        return users;
    }

    public User findById(final Long id) throws SQLException {
        final var sql = "select id, account, password, email from users where id = ?";
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, id)
                                      .orElseThrow(() -> new RuntimeException("찾는 사용자가 존재하지 않습니다."));

        log.debug("query : {}", sql);

        return user;
    }

    public User findByAccount(final String account) throws SQLException {
        final var sql = "select id, account, password, email from users where account = ?";
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, account)
                                      .orElseThrow(() -> new RuntimeException("찾는 사용자가 존재하지 않습니다."));

        log.debug("query : {}", sql);

        return user;
    }
}
