package com.techcourse.dao;

import com.techcourse.dao.exception.UserNotFoundException;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

    private static final RowMapper<User> userRowMapper = resultSet -> {
        final Long id = resultSet.getLong("id");
        final String account = resultSet.getString("account");
        final String password = resultSet.getString("password");
        final String email = resultSet.getString("email");

        return new User(id, account, password, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.executeQuery(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";

        jdbcTemplate.executeQuery(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.executeQueryForList(sql, userRowMapper);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.executeQueryForObject(sql, userRowMapper, id)
                           .orElseThrow(() -> new UserNotFoundException("지정한 id에 대한 User를 찾을 수 없습니다."));
    }

    public User findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.executeQueryForObject(sql, userRowMapper, account)
                           .orElseThrow(() -> new UserNotFoundException("지정한 account에 대한 User를 찾을 수 없습니다."));
    }
}
