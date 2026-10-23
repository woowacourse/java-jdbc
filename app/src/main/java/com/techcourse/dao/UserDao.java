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

    private static final RowMapper USER_ROW_MAPPER = new RowMapper() {
        @Override
        public Object mapRow(ResultSet resultSet) throws SQLException {
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
        jdbcTemplate.update(sql, new Object[] {user.getAccount(), user.getPassword(), user.getEmail()});
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, new Object[] {user.getAccount(), user.getPassword(), user.getEmail(), user.getId()});
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        List<Object> results = jdbcTemplate.query(sql, new Object[0], USER_ROW_MAPPER);
        List<User> users = new ArrayList<>();
        for (Object result : results) {
            users.add((User) result);
        }
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.queryForObject(sql, new Object[] {id}, USER_ROW_MAPPER);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.queryForObject(sql, new Object[] {account}, USER_ROW_MAPPER);
    }
}
