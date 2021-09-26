package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

    public static void insert(User user) {
        log.info("UserDao insert Method");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public static void update(User user) {
        log.info("UserDao update Method");
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public static List<User> findAll() {
        log.info("UserDao findAll Method");
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, userRowMapper());
    }

    public static User findById(Long id) {
        log.info("UserDao findById Method");
        final String sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper(), id);
    }

    public static User findByAccount(String account) {
        log.info("UserDao findByAccount Method");
        final String sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper(), account);
    }

    public static RowMapper<User> userRowMapper() {
        return (rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        ));
    }
}
