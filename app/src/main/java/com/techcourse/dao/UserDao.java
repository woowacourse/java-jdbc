package com.techcourse.dao;

import com.interface21.jdbc.ResultSetMapper;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final ResultSetMapper<ResultSet, User> userMapper = resultSet ->
            new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.queryForList(sql, userMapper).stream()
                .map(result -> (User) result)
                .toList();
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Optional<Object> result = jdbcTemplate.queryForObject(sql, userMapper, id);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("해당 id의 회원이 존재하지 않습니다");
        }

        return (User) result.get();
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        Optional<Object> result = jdbcTemplate.queryForObject(sql, userMapper, account);

        if (result.isEmpty()) {
            throw new IllegalArgumentException("해당 account의 회원이 존재하지 않습니다");
        }

        return (User) result.get();
    }
}
