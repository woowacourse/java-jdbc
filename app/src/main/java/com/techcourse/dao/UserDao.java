package com.techcourse.dao;

import com.techcourse.config.JdbcTemplateConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    public UserDao() {
        this.jdbcTemplate = JdbcTemplateConfig.jdbcTemplate();
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        printQueryLog(sql);
        jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        printQueryLog(sql);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<User> findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        printQueryLog(sql);
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
    }

    public Optional<User> findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";
        printQueryLog(sql);
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, account));
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        printQueryLog(sql);
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public void deleteAll() {
        final String sql = "delete from users";
        printQueryLog(sql);
        jdbcTemplate.delete(sql);
    }

    private void printQueryLog(String sql) {
        log.info("query : {}", sql);
    }
}
