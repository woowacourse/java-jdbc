package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final RowMapper USER_ROW_MAPPER = new RowMapper() {
        @Override
        public Object mapRow(final ResultSet resultSet) throws SQLException {
            return new User(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4));
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
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        final List<User> users = new ArrayList<>();
        for (final Object result : jdbcTemplate.query(sql, USER_ROW_MAPPER)) {
            users.add((User) result);
        }
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
