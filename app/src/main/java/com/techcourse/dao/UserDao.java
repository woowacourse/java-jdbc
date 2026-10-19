package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"), rs.getString("account"), rs.getString("password"), rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        jdbcTemplate.update("update users set account = ?, password = ?, email = ? where id = ?",
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        return jdbcTemplate.query("select id, account, password, email from users", USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        return jdbcTemplate.queryForObject("select id, account, password, email from users where id = ?",
                USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        return jdbcTemplate.queryForObject("select id, account, password, email from users where account = ?",
                USER_ROW_MAPPER, account);
    }
}
