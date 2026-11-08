package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final String UPDATE_SQL =
            "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
    private static final String FIND_BY_ID_SQL =
            "select id, account, password, email from users where id = ?";

    private static final RowMapper<User> USER_ROW_MAPPER =
            rs -> new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.update(UPDATE_SQL, updateArgs(user));
    }

    public void update(final Connection connection, final User user) {
        jdbcTemplate.update(connection, UPDATE_SQL, updateArgs(user));
    }

    public List<User> findAll() {
        final var sql = "SELECT id, account, password, email FROM users";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject(FIND_BY_ID_SQL, USER_ROW_MAPPER, id);
    }

    public User findById(final Connection connection, final Long id) {
        return jdbcTemplate.queryForObject(connection, FIND_BY_ID_SQL, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "SELECT id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }

    private Object[] updateArgs(final User user) {
        return new Object[]{
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        };
    }
}
