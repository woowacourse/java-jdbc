package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));
    private static final String UPDATE_SQL = "update users set account = ?, password = ?, email = ? where id = ?";
    private static final String FIND_BY_ID_SQL = "select id, account, password, email from users where id = ?";

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
        final var updatedRows = jdbcTemplate.update(
                UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        validateUpdated(updatedRows, user);
    }

    public void update(final Connection connection, final User user) {
        final var updatedRows = jdbcTemplate.update(
                connection, UPDATE_SQL, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        validateUpdated(updatedRows, user);
    }

    private void validateUpdated(final int updatedRows, final User user) {
        if (updatedRows == 0) {
            throw new DataAccessException("수정할 사용자가 없습니다. id: " + user.getId());
        }
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject(FIND_BY_ID_SQL, USER_ROW_MAPPER, id);
    }

    public User findById(final Connection connection, final Long id) {
        return jdbcTemplate.queryForObject(connection, FIND_BY_ID_SQL, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
