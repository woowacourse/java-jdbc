package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class UserDao {

    private static final String INSERT_SQL = "insert into users (account, password, email) values (?, ?, ?)";
    private static final String UPDATE_SQL = "update users set password = ? where id = ?";
    private static final String SELECT_ALL_SQL = "select id, account, password, email from users";
    private static final String SELECT_BY_ID_SQL = SELECT_ALL_SQL + " where id = ?";
    private static final String SELECT_BY_ACCOUNT_SQL = SELECT_ALL_SQL + " where account = ?";
    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update(INSERT_SQL, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public int update(final User user) {
        return jdbcTemplate.update(UPDATE_SQL, user.getPassword(), user.getId());
    }

    public int update(final Connection connection, final User user) {
        return jdbcTemplate.update(connection, UPDATE_SQL, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, USER_ROW_MAPPER);
    }

    public Optional<User> findById(final Long id) {
        return jdbcTemplate.queryForObject(SELECT_BY_ID_SQL, USER_ROW_MAPPER, id);
    }

    public Optional<User> findByAccount(final String account) {
        return jdbcTemplate.queryForObject(SELECT_BY_ACCOUNT_SQL, USER_ROW_MAPPER, account);
    }
}
