package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.Mapper;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Mapper<User> USER_MAPPER = rs -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForObject(sql, rs -> {
            final List<User> users = new ArrayList<>();
            do {
                users.add(USER_MAPPER.map(rs));
            } while (rs.next());
            return users;
        });
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, USER_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, USER_MAPPER, account);
    }
}
