package com.techcourse.dao;

import com.techcourse.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4)
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();
        jdbcTemplate.update(sql, account, password, email);
    }

    public void update(final User user) {
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";

        final String account = user.getAccount();
        final String password = user.getPassword();
        final String email = user.getEmail();
        final Long id = user.getId();
        jdbcTemplate.update(sql, account, password, email, id);
    }

    public List<User> findAll() {
        final String sql = "SELECT id, account, password, email FROM users";
        return jdbcTemplate.queryForObjectsWithParameter(sql, rowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Optional<User> user = jdbcTemplate.queryForObjectWithParameter(sql, rowMapper, id);

        return user.orElseThrow(() -> new IllegalArgumentException("해당 아이디로 조회되는 유저가 없습니다."));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        Optional<User> user = jdbcTemplate.queryForObjectWithParameter(sql, rowMapper, account);

        return user.orElseThrow(() -> new IllegalArgumentException("해당 계정으로 조회되는 유로가 없습니다."));
    }
}
