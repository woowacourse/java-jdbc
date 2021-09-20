package com.techcourse.dao;

import com.techcourse.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(
            sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );
    }

    public void update(User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(
            sql,
            user.getAccount(),
            user.getPassword(),
            user.getEmail(),
            user.getId()
        );
    }

    public List<User> findAll() {
        String sql = "select id, account, password, email from users";
        return (List<User>) jdbcTemplate.query(sql,
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
            }
        );
    }

    public User findById(Long id) {
        String sql = "select id, account, password, email from users where id = ?";
        return (User) jdbcTemplate.query(
            sql,
            resultSet -> {
                if (resultSet.next()) {
                    return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4));
                }
                return null;
            },
            id
        );
    }

    public User findByAccount(String account) {
        String sql = "select id, account, password, email from users where account = ?";
        return (User) jdbcTemplate.query(
            sql,
            resultSet -> {
                if (resultSet.next()) {
                    return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4));
                }
                return null;
            },
            account
        );
    }
}
