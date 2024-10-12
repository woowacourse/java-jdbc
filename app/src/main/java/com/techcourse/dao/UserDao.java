package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet) ->
            new User(resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, (preparedStatement) -> {
            preparedStatement.setObject(1, user.getAccount());
            preparedStatement.setObject(2, user.getPassword());
            preparedStatement.setObject(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, (preparedStatement) -> {
            preparedStatement.setObject(1, user.getAccount());
            preparedStatement.setObject(2, user.getPassword());
            preparedStatement.setObject(3, user.getEmail());
            preparedStatement.setObject(4, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, (preparedStatement) -> {
        }, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, (preparedStatement) -> {
            preparedStatement.setObject(1, id);
        }, USER_ROW_MAPPER);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, (preparedStatement) -> {
            preparedStatement.setObject(1, account);
        }, USER_ROW_MAPPER);
    }

    public void update(Connection connection, User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, connection, (preparedStatement) -> {
            preparedStatement.setObject(1, user.getAccount());
            preparedStatement.setObject(2, user.getPassword());
            preparedStatement.setObject(3, user.getEmail());
            preparedStatement.setObject(4, user.getId());
        });
    }
}
