package com.techcourse.dao;

import com.techcourse.domain.User;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate dataSource;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        dataSource.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        dataSource.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        final List<User> result = dataSource.query(
                sql,
                (rs, rowNum) -> new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                )
        );
        return result;
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        final User result = dataSource.queryForObject(
                sql,
                (rs, rowNum) -> new User(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    ),
                id
        );
        return result;
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        final User result = dataSource.queryForObject(
                sql,
                (rs, rowNum) -> new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                ),
                account
        );
        return result;
    }

    public void reset() {
        final var sql = "truncate table users restart identity";
        dataSource.update(sql);
    }
}
