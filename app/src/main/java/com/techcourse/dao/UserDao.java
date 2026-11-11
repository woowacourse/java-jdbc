package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.executeUpdate(sql, List.of(
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        ));
    }

    public void update(final User user) {
        final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        jdbcTemplate.executeUpdate(sql, List.of(
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        ));
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Map<String, Object> result = jdbcTemplate.query(sql, List.of(id))
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("id를 찾을 수 없습니다"));

        return new User(
                ((Number) result.get("id")).longValue(),
                (String) result.get("account"),
                (String) result.get("password"),
                (String) result.get("email")
        );
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
