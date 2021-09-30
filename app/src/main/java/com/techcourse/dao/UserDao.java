package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {

        String query = "insert into users (account, password, email) values (?, ?, ?)";

        String account = user.getAccount();
        String password = user.getPassword();
        String email = user.getEmail();

        jdbcTemplate.update(query, account, password, email);
    }

    public void update(User user) {

        String query = "update users set account=?, password=?, email=? where id=?";

        String account = user.getAccount();
        String password = user.getPassword();
        String email = user.getEmail();
        Long id = user.getId();

        jdbcTemplate.update(query, account, password, email, id);
    }

    public List<User> findAll() {

        String query = "select id, account, password, email from users";

        return jdbcTemplate.query(query, USER_ROW_MAPPER);
    }

    public User findById(Long id) {
        String query = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(query, USER_ROW_MAPPER, id);
    }

    public User findByAccount(String account) {
        String query = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(query, USER_ROW_MAPPER, account);
    }
}
