package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.AbstractJdbcTemplate;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final RowMapper<User> userRowMapper = rs -> new User(
        rs.getLong("id"),
        rs.getString("account"),
        rs.getString("password"),
        rs.getString("email")
    );



    public void insert(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }


    public void update(User user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "update users set password = ?, email = ? where account = ? ";
        jdbcTemplate.update(sql, user.getPassword(), user.getEmail(), user.getAccount());
    }

    public List<User> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select id, account, password, email from users";
        return (List<User>) jdbcTemplate.query(sql, userRowMapper);
    }

    public User findById(Long id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.query(sql, userRowMapper, id);
    }

    public User findByAccount(String account) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.query(sql, userRowMapper, account);
    }

}
