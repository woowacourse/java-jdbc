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
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getPassword());
            pstmt.setLong(2, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                """;

        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        List<User> results = jdbcTemplate.query(sql, pstmt -> pstmt.setLong(1, id), userRowMapper);

        return results.stream()
                .findFirst();
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = """
                SELECT id, account, password, email
                FROM users
                WHERE account = ?
                """;

        List<User> results = jdbcTemplate.query(sql, pstmt -> pstmt.setString(1, account), userRowMapper);

        return results.stream()
                .findFirst();
    }
}
