package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll(final Object... args) {
        final var sql = "select * from users";
        final List<Map<String, Object>> objectList = jdbcTemplate.queryForList(sql, args);

        final List<User> userList = new ArrayList<>();
        for (Map<String, Object> object : objectList) {
            final User user = mapToUser(object);
            userList.add(user);
        }
        return userList;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final Map<String, Object> object = jdbcTemplate.queryForObject(sql, id);
        return mapToUser(object);
    }

    public User findByAccount(final String account) {
        final var sql = "select * from users where account = ?";
        final Map<String, Object> object = jdbcTemplate.queryForObject(sql, account);
        return mapToUser(object);
    }

    private User mapToUser(final Map<String, Object> result) {
        return new User(
                (Long) result.get("ID"),
                (String) result.get("ACCOUNT"),
                (String) result.get("PASSWORD"),
                (String) result.get("EMAIL")
        );
    }
}
