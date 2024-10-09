package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
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


    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.write(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        String sql = "UPDATE users SET account = ?, password = ?, email = ?";
        jdbcTemplate.write(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.readAll(sql, rowMapper);
    }

    public User findById(final Long id) {
        String sql = "SELECT * FROM users where id = ?";
        return jdbcTemplate.read(sql, rowMapper, id);
    }

    public User findByAccount(final String account) {
        String sql = "SELECT * FROM users WHERE account = ?";
        return jdbcTemplate.read(sql, rowMapper, account);
    }
}
