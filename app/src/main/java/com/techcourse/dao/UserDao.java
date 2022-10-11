package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.ResultSetWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final ResultSetWrapper<User> userResult = (resultSet) -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sqlFormat = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sqlFormat, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sqlFormat = "update users set account=?, password=?,email=? where id = ?";
        jdbcTemplate.update(sqlFormat, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sqlFormat = "select id, account, password, email from users";
        return jdbcTemplate.queryForList(sqlFormat, userResult);
    }

    public User findById(final Long id) {
        final var sqlFormat = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.query(sqlFormat, userResult, id);
    }

    public User findByAccount(final String account) {
        final var sqlFormat = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.query(sqlFormat, userResult, account);
    }
}
