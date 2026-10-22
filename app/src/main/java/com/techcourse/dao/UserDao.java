package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        new JdbcOperationTemplate(jdbcTemplate) {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected Object[] createParameters() {
                return new Object[]{user.getAccount(), user.getPassword(), user.getEmail()};
            }
        }.update();
    }

    public void update(final User user) {
        new JdbcOperationTemplate(jdbcTemplate) {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected Object[] createParameters() {
                return new Object[]{user.getAccount(), user.getPassword(), user.getEmail(), user.getId()};
            }
        }.update();
    }

    public List<User> findAll() {
        return new JdbcOperationTemplate(jdbcTemplate) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users";
            }
        }.query(USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        return new JdbcOperationTemplate(jdbcTemplate) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where id = ?";
            }

            @Override
            protected Object[] createParameters() {
                return new Object[]{id};
            }
        }.queryForObject(USER_ROW_MAPPER);
    }

    public User findByAccount(final String account) {
        return new JdbcOperationTemplate(jdbcTemplate) {
            @Override
            protected String createQuery() {
                return "select id, account, password, email from users where account = ?";
            }

            @Override
            protected Object[] createParameters() {
                return new Object[]{account};
            }
        }.queryForObject(USER_ROW_MAPPER);
    }
}
