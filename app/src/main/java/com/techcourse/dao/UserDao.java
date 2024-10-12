package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = (resultSet ->
            new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            )
    );

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Connection connection, User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.write(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(Connection connection, User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ?";
        jdbcTemplate.write(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll(Connection connection) {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.readAll(connection, sql, rowMapper);
    }

    public User findById(Connection connection, Long id) {
        String sql = "SELECT * FROM users where id = ?";
        return jdbcTemplate.read(connection, sql, rowMapper, id);
    }

    public User findByAccount(Connection connection, String account) {
        String sql = "SELECT * FROM users WHERE account = ?";
        return jdbcTemplate.read(connection, sql, rowMapper, account);
    }
}
