package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcOperations;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class UserDao {

    private static final RowMapper<User> USER_MAPPER = resultSet -> new User(
        resultSet.getLong("id"),
        resultSet.getString("account"),
        resultSet.getString("password"),
        resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final int affectedRows = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
        requireOneRow(affectedRows);
    }

    public int update(User user) {
        return update(jdbcTemplate::update, user);
    }

    public int update(JdbcOperations jdbc, User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        return jdbc.update(
            sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.query("select id, account, password, email from users", USER_MAPPER);
    }

    public Optional<User> findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_MAPPER, id);
    }

    public Optional<User> findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_MAPPER, account);
    }

    private void requireOneRow(final int affectedRows) {
        if (affectedRows != 1) {
            throw new DataAccessException("Expected one affected user but found " + affectedRows);
        }
    }
}
