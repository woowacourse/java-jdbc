package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, new Object[]{
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        });
    }

    public void update(final User user) {
        final var sql = """
                UPDATE users
                SET account = ?, password = ?, email = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, new Object[]{
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        final List<Object> rows = jdbcTemplate.query(sql, new Object[]{}, userRowMapper());
        final List<User> users = new ArrayList<>();
        for (Object row : rows) {
            users.add((User) row);
        }
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        final List<Object> rows = jdbcTemplate.query(sql, new Object[]{id}, userRowMapper());
        if (rows.isEmpty()) {
            return null;
        }
        return (User) rows.getFirst();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        final List<Object> rows = jdbcTemplate.query(sql, new Object[]{account}, userRowMapper());
        if (rows.isEmpty()) {
            return null;
        }
        return (User) rows.getFirst();
    }

    private RowMapper userRowMapper() {
        return new RowMapper() {
            @Override
            public Object mapRow(final ResultSet resultSet) throws SQLException {
                return new User(
                        resultSet.getLong("id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email")
                );
            }
        };
    }
}
