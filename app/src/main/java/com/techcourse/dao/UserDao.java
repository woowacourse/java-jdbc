package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.result.MultiSelectResult;
import com.interface21.jdbc.result.SingleSelectResult;
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
        jdbcTemplate.command(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.command(sql,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        final MultiSelectResult queryResult = jdbcTemplate.queryMulti(sql);
        return queryResult.stream()
                .map(this::mapperToUser)
                .toList();
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final SingleSelectResult queryResult = jdbcTemplate.querySingle(sql, id);
        return new User(
                queryResult.getColumnValue("id"),
                queryResult.getColumnValue("account"),
                queryResult.getColumnValue("password"),
                queryResult.getColumnValue("email")
        );
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final SingleSelectResult queryResult = jdbcTemplate.querySingle(sql, account);
        return mapperToUser(queryResult);
    }

    private User mapperToUser(final SingleSelectResult queryResult) {
        return new User(
                queryResult.getColumnValue("id"),
                queryResult.getColumnValue("account"),
                queryResult.getColumnValue("password"),
                queryResult.getColumnValue("email")
        );
    }
}
