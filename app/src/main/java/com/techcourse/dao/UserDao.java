package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.JdbcTemplate;
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
    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        this.jdbcTemplate.executeUpdate(
            sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );
        log.debug("query : {}", sql);
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        this.jdbcTemplate.executeUpdate(
            sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail(),
            user.getId()
        );
        log.debug("query : {}", sql);
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
                );
            }
        );
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return this.jdbcTemplate.query(
            sql,
            (rs) -> {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            },
            id
        );
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return this.jdbcTemplate.query(
            sql,
            (rs) -> {
                if (rs.next()) {
                    return new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                }
                return null;
            },
            account
        );
    }
}
