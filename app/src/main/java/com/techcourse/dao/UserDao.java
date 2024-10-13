package com.techcourse.dao;

import com.interface21.jdbc.core.extractor.ExtractionRule;
import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void insert(User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final var sql = "update users set account=?, password=?, email=? where id=?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(User.class, sql);
    }

    public User findById(Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        ExtractionRule<User> rule = rs -> new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("account"),
                rs.getString("email")
        );
        return jdbcTemplate.queryOne(rule, sql, id);
    }

    public User findByAccount(String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryOne(User.class, sql, account);
    }
}
