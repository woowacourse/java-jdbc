package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.PreparedStatementSetter;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

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

        return jdbcTemplate.query(query, rs -> {
            List<User> result = new ArrayList<>();

            while(rs.next()) {
                result.add(new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }

            return result;
        });
    }

    public User findById(Long id) {
        String query = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.query(query, rs -> {
            if(rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }

            return null;
        }, id);
    }

    public User findByAccount(String account) {
        String query = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.query(query, rs -> {
            if(rs.next()) {
                return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
            }

            return null;
        }, account);
    }
}
