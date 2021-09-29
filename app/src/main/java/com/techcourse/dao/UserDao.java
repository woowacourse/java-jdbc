package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource) {
        };
    }

    public void insert(User user) throws SQLException {
        jdbcTemplate.update(
            "insert into users (account, password, email) values (?, ?, ?)",
            pstmt -> {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
            });
    }

    public void update(User user) throws SQLException {
        jdbcTemplate.update(
            "update users set account = ?, password = ?, email = ?  where id = ?",
            pstmt -> {
                pstmt.setString(1, user.getAccount());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getEmail());
                pstmt.setLong(4, user.getId());
            });
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        return (List<User>) jdbcTemplate.query(
            "select id, account, password, email from users",
            pstmt -> {

            },
            rs -> users.add(
                new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4))
            )
        );
    }

    public User findById(Long id) throws SQLException {
        return (User) jdbcTemplate.queryForObject(
            "select id, account, password, email from users where id = ?",
            pstmt -> {
                pstmt.setLong(1, id);
            },
            rs -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4))
        );
    }

    public User findByAccount(String account) throws SQLException {
        return (User) jdbcTemplate.queryForObject(
            "select id, account, password, email from users where account = ?",
            pstmt -> {
                pstmt.setString(1, account);
            },
            rs -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4))
        );
    }
}
