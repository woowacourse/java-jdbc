package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        return new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    };

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = """
                INSERT INTO users (account, password, email)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        final var sql = """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                user.getPassword(),
                user.getId()
        );
    }

    public List<User> findAll() {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                """;

        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(final Long id) {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                WHERE id = ?
                """;

        List<User> results = jdbcTemplate.query(sql, userRowMapper, id);

        return results.stream()
                .findFirst();
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                WHERE account = ?
                """;

        List<User> results = jdbcTemplate.query(sql, userRowMapper, account);

        return results.stream()
                .findFirst();
    }
}
