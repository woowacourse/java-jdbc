package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;

public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    private final SelectJdbcTemplate selectJdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.selectJdbcTemplate = new SelectJdbcTemplate(jdbcTemplate);
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
        return selectJdbcTemplate.findAll();
    }

    public User findById(final Long id) {
        return selectJdbcTemplate.findById(id);
    }

    public User findByAccount(final String account) {
        return selectJdbcTemplate.findByAccount(account);
    }
}
