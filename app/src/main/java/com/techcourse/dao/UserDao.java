package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetGetter;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(
                List.of(user.getAccount(), user.getPassword(), user.getEmail()));
        jdbcTemplate.execute(sql, preparedStatementSetter);
    }

    public void update(final User user) {
        // todo
    }

    public List<User> findAll() {
        // todo
        return null;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        Map<String, Class<?>> values = new HashMap<>();
        values.put("id", Long.class);
        values.put("account", String.class);
        values.put("password", String.class);
        values.put("email", String.class);

        ResultSetGetter<User> userResultSetGetter = new ResultSetGetter<>(values, User.class);
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter(List.of(id));
        return (User) jdbcTemplate.find(sql, preparedStatementSetter, userResultSetGetter);
    }

    public User findByAccount(final String account) {
        // todo
        return null;
    }
}
