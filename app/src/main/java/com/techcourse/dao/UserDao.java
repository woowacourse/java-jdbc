package com.techcourse.dao;

import com.interface21.context.stereotype.Component;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.mapper.UserMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Component
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.queryForUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        return jdbcTemplate.queryForResultList("SELECT * FROM users", UserMapper.USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        Optional<User> user = jdbcTemplate.queryForResult(sql, UserMapper.USER_ROW_MAPPER, id);
        return user.orElseThrow(IllegalArgumentException::new);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.queryForUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForResult(sql, UserMapper.USER_ROW_MAPPER, account);
    }
}
