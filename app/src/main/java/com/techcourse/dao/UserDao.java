package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.NoSuchElementException;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set (account, password, email) = (?, ?, ?) where id = ?";

        jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id)
                .orElseThrow(() -> new NoSuchElementException("User 가 존재하지 않습니다."));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account)
                .orElseThrow(() -> new NoSuchElementException("User 가 존재하지 않습니다."));
    }
}
