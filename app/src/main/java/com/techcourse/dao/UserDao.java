package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetObjectMapper;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final String USER_ACCOUNT_COLUMN = "account";
    private static final String USER_PASSWORD_COLUMN = "password";
    private static final String USER_EMAIL_COLUMN = "email";
    private static final String USER_ID_COLUMN = "id";
    private static final ResultSetObjectMapper<User> USER_OBJECT_MAPPER = resultSet -> {
        final long userId = resultSet.getLong(USER_ID_COLUMN);
        final String userAccount = resultSet.getString(USER_ACCOUNT_COLUMN);
        final String userPassword = resultSet.getString(USER_PASSWORD_COLUMN);
        final String userEmail = resultSet.getString(USER_EMAIL_COLUMN);
        return new User(userId, userAccount, userPassword, userEmail);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.simpleExecute(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.simpleExecute(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.executeQueryForObject(sql, USER_OBJECT_MAPPER, id);
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.executeQueryForObjects(sql, USER_OBJECT_MAPPER);
    }

    public User findByAccount(final String account) {
        final String sql = "select * from users where account=?";
        return jdbcTemplate.executeQueryForObject(sql, USER_OBJECT_MAPPER, account);
    }
}
