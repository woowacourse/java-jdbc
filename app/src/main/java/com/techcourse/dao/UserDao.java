package com.techcourse.dao;

import com.interface21.jdbc.core.extractor.ExtractionRule;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final ExtractionRule<User> EXTRACT_RULE
            = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void insert(Connection connection, User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(Connection connection, User user) {
        final var sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(connection, sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll(Connection connection) {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(connection, User.class, sql);
    }

    public User findById(Connection connection, Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryOne(connection, EXTRACT_RULE, sql, id);
    }

    public User findByAccount(Connection connection, String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryOne(connection, User.class, sql, account);
    }
}
