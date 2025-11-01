package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ResultMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private static final ResultMapper<User> USER_MAPPER = resultSet -> new User(
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
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );
    }

    public void update(final User user) {
        jdbcTemplate.update(
                "update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }

    public List<User> findAll() {
        return jdbcTemplate.selectList(
                "select id, account, password, email from users",
                USER_MAPPER
        );
    }

    public User findById(final Long id) {
        return jdbcTemplate.selectOne(
                "select id, account, password, email from users where id = ?",
                USER_MAPPER,
                id
        );
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.selectOne(
                "select id, account, password, email from users where account = ?",
                USER_MAPPER,
                account
        );
    }
}
