package com.techcourse.dao;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };
    }

    public void insert(User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return (List<User>) jdbcTemplate.query(sql, pstmt -> {},
                resultSet -> {
                    List<User> users = new ArrayList<>();
                    if (resultSet.next()) {
                        users.add(new User(
                                resultSet.getLong(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4)));
                    }
                    return users;
                });
    }

    public User findById(Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return (User) jdbcTemplate.query(sql, pstmt -> pstmt.setLong(1, id),
                resultSet -> {
                    if (resultSet.next()) {
                        return new User(
                                resultSet.getLong(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4));
                    }
                    return null;
                });
    }

    public User findByAccount(String account) {
        final String sql = "select id, account, password, email from users where account = ?";

        return (User) jdbcTemplate.query(sql, pstmt -> pstmt.setString(1, account),
                resultSet -> {
                    if (resultSet.next()) {
                        return new User(
                                resultSet.getLong(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4));
                    }
                    return null;
                });
    }
}
