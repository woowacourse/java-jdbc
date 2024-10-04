package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;

import java.util.Optional;
import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert int users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
        });
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email =? where id = ?";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sql, USER_ROW_MAPPER);
    }

    public Optional<User> findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, preparedStatement ->
                preparedStatement.setLong(1, id));
    }

    public Optional<User> findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, preparedStatement ->
                preparedStatement.setString(1, account));
    }
}
