package com.techcourse.dao;

import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ObjectMapper;
import com.interface21.jdbc.core.OrderedSetter;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.User;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final ObjectMapper<User> USER_OBJECT_MAPPER = (resultSet, rowNum) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));
    private static final PreparedStatementSetter ORDERED_SETTER = new OrderedSetter();

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(User user) {
        jdbcTemplate.execute(ORDERED_SETTER,
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        jdbcTemplate.execute(ORDERED_SETTER,
                "update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.getResults(ORDERED_SETTER, "select id, account, password, email from users",
                USER_OBJECT_MAPPER);
    }

    public User findById(Long id) {
        return jdbcTemplate.getResult(ORDERED_SETTER,
                "select id, account, password, email from users where id = ?",
                USER_OBJECT_MAPPER,
                id);
    }

    public User findByAccount(String account) {
        return jdbcTemplate.getResult(ORDERED_SETTER,
                "select id, account, password, email from users where account = ?",
                USER_OBJECT_MAPPER,
                account);
    }
}
