package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.FindJdbcTemplate;
import nextstep.jdbc.InsertJdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final InsertJdbcTemplate insertJdbcTemplate;
    private final FindJdbcTemplate findJdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.insertJdbcTemplate = new InsertJdbcTemplate(dataSource);
        this.findJdbcTemplate = new FindJdbcTemplate(dataSource);
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        log.info("query : {}", sql);

        insertJdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(User user) {
        final String sql = "update users set account=?, password=?, email=? where id=?";
        log.info("query : {}", sql);

        insertJdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        log.info("query : {}", sql);

        return findJdbcTemplate.query(sql, new UserMapper());
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";
        log.info("query : {}", sql);

        return findJdbcTemplate.queryForObject(sql, new UserMapper(), id);
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account= ?";
        log.info("query : {}", sql);

        return findJdbcTemplate.queryForObject(sql, new UserMapper(), account);
    }
}
