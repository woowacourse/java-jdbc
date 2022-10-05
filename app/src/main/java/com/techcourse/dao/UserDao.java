package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.Parameters;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        Parameters parameters = setInsertParameter(user);
        jdbcTemplate.update(sql, parameters);
    }

    private Parameters setInsertParameter(User user) {
        Parameters parameters = new Parameters();
        parameters.addParam(user.getAccount());
        parameters.addParam(user.getPassword());
        parameters.addParam(user.getEmail());
        return parameters;
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        Parameters parameters = setUpdateParameter(user);
        jdbcTemplate.update(sql, parameters);
    }

    private Parameters setUpdateParameter(User user) {
        Parameters parameters = new Parameters();
        parameters.addParam(user.getAccount());
        parameters.addParam(user.getPassword());
        parameters.addParam(user.getEmail());
        parameters.addParam(user.getId());
        return parameters;
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, new Parameters(), rowMapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        Parameters parameters = new Parameters();
        parameters.addParam(id);
        return jdbcTemplate.queryForObject(sql, parameters, rowMapper);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        Parameters parameters = new Parameters();
        parameters.addParam(account);
        return jdbcTemplate.queryForObject(sql, parameters, rowMapper);
    }

    private final RowMapper<User> rowMapper = rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );
}
