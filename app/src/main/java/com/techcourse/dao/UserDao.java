package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.executeUpdate(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select * from users";
        return jdbcTemplate.queryForList(sql, USER_ROW_MAPPER);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ? limit 1";
        List<User> users = jdbcTemplate.queryForList(sql, USER_ROW_MAPPER, id);

        return getOptionalResult(users);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        List<User> users = jdbcTemplate.queryForList(sql, USER_ROW_MAPPER, account);

        return getOptionalResult(users);
    }

    private Optional<User> getOptionalResult(List<User> users) {
        if (users.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(users.getFirst());
    }
}
