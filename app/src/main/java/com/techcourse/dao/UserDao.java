package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final JdbcOperationTemplate insertJdbcTemplate;
    private final JdbcOperationTemplate updateJdbcTemplate;

    private static final RowMapper<User> USER_ROW_MAPPER = rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            );


    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertJdbcTemplate = new JdbcOperationTemplate(jdbcTemplate) {
            @Override
            protected String createQuery() {
                return "insert into users (account, password, email) values (?, ?, ?)";
            }

            @Override
            protected Object[] createParameters(final User user) {
                return new Object[]{user.getAccount(), user.getPassword(), user.getEmail()};
            }
        };
        this.updateJdbcTemplate = new JdbcOperationTemplate(jdbcTemplate) {
            @Override
            protected String createQuery() {
                return "update users set account = ?, password = ?, email = ? where id = ?";
            }

            @Override
            protected Object[] createParameters(final User user) {
                return new Object[]{user.getAccount(), user.getPassword(), user.getEmail(), user.getId()};
            }
        };
    }

    public void insert(final User user) {
        insertJdbcTemplate.update(user);
    }

    public void update(final User user) {
        updateJdbcTemplate.update(user);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }
}
