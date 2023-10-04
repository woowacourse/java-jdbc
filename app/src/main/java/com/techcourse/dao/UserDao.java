package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final int updatedRows = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        if (updatedRows < 1) {
            throw new RuntimeException("저장된 데이터가 없습니다.");
        }

        log.debug("query : {}", sql);
    }

    public void update(final User user) {
        final var sql = "update users set (account, password, email) = (?, ?, ?)";
        final int updatedRows = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        if (updatedRows < 1) {
            throw new RuntimeException("수정된 데이터가 없습니다.");
        }

        log.debug("query : {}", sql);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        final List<User> users = jdbcTemplate.query(sql, userRowMapper);

        log.debug("query : {}", sql);

        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, id)
                                      .orElseThrow(() -> new RuntimeException("찾는 사용자가 존재하지 않습니다."));

        log.debug("query : {}", sql);

        return user;
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, account)
                                      .orElseThrow(() -> new RuntimeException("찾는 사용자가 존재하지 않습니다."));

        log.debug("query : {}", sql);

        return user;
    }
}
