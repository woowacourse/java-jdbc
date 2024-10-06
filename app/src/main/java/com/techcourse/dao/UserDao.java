package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.result.SelectMultiResult;
import com.interface21.jdbc.result.SelectSingleResult;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.write(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.write(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        final SelectMultiResult queryResult = jdbcTemplate.selectMulti(sql);
        return queryResult.stream()
                .map(this::mapperToUser)
                .toList();
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final SelectSingleResult queryResult = jdbcTemplate.selectOne(sql, id);
        return new User(
                queryResult.getColumnValue("id"),
                queryResult.getColumnValue("account"),
                queryResult.getColumnValue("password"),
                queryResult.getColumnValue("email")
        );
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final SelectSingleResult queryResult = jdbcTemplate.selectOne(sql, account);
        return mapperToUser(queryResult);
    }

    private User mapperToUser(final SelectSingleResult queryResult) {
        return new User(
                queryResult.getColumnValue("id"),
                queryResult.getColumnValue("account"),
                queryResult.getColumnValue("password"),
                queryResult.getColumnValue("email")
        );
    }
}
