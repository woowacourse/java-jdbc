package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<>() {
        @Override
        public User mapRow(final ResultSet resultSet) throws SQLException {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final Object[] parameters = {user.getAccount(), user.getPassword(), user.getEmail()};

        jdbcTemplate.update(sql, parameters);
    }

    public void update(final User user) {
        final var sql = "update users set password = ? where id = ?";
        final Object[] parameters = {user.getPassword(), user.getId()};

        jdbcTemplate.update(sql, parameters);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, new Object[0], USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final var rows = jdbcTemplate.query(sql, new Object[]{id}, USER_ROW_MAPPER);

        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final var rows = jdbcTemplate.query(sql, new Object[]{account}, USER_ROW_MAPPER);

        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }
}
