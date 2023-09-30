package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final RowMapper<User> rowMapper = resultSet -> {
        Long id = resultSet.getLong("id");
        String account = resultSet.getString("account");
        String password = resultSet.getString("password");
        String email = resultSet.getString("email");
        return new User(id, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rowMapper);
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        Optional<User> user = jdbcTemplate.queryForObject(sql, rowMapper, id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("해당하는 유저가 존재하지 않습니다.");
        }
        return user.get();
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        Optional<User> user = jdbcTemplate.queryForObject(sql, rowMapper, account);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("해당하는 유저가 존재하지 않습니다.");
        }
        return user.get();
    }
}
