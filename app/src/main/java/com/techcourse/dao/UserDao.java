package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final UserMaker maker;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.maker = new UserMaker();
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        List<Object> paramList = new ArrayList<>();
        paramList.add(user.getAccount());
        paramList.add(user.getPassword());
        paramList.add(user.getEmail());

        jdbcTemplate.executeQuery(sql, paramList);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        List<Object> paramList = new ArrayList<>();
        paramList.add(user.getAccount());
        paramList.add(user.getPassword());
        paramList.add(user.getEmail());
        paramList.add(user.getId());

        jdbcTemplate.executeQuery(sql, paramList);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        List<Object> paramList = new ArrayList<>();

        List<Object> results = jdbcTemplate.executeQueryForObjects(sql, paramList, maker);

        List<User> users = new ArrayList<>();
        for (Object result : results) {
            if (result instanceof User) {
                users.add((User) result);
            }
        }
        return users;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        List<Object> paramList = new ArrayList<>();
        paramList.add(id);

        return (User) jdbcTemplate.executeQueryForObject(sql, paramList, maker);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        List<Object> paramList = new ArrayList<>();
        paramList.add(account);

        return (User) jdbcTemplate.executeQueryForObject(sql, paramList, maker);
    }
}
