package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

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
        List<Map<String, Object>> rows = jdbcTemplate.queryForResultList("SELECT * FROM users");
        return rows.stream()
                .map(this::mapToUser)
                .toList();
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        Optional<Map<String, Object>> stringObjectMap = jdbcTemplate.queryForResult(sql, id);
        return stringObjectMap.map(this::mapToUser).orElse(null);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.queryForUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        Optional<Map<String, Object>> stringObjectMap = jdbcTemplate.queryForResult(sql, account);
        return stringObjectMap.map(this::mapToUser).orElse(null);
    }

    private User mapToUser(Map<String, Object> row) {
        return new User(
                (Long) row.get("ID"),
                (String) row.get("ACCOUNT"),
                (String) row.get("PASSWORD"),
                (String) row.get("EMAIL")
        );
    }
}
