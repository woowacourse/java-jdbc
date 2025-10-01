package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.executeUpdate(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";

        jdbcTemplate.executeUpdate(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rs ->
            new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            )
        );
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, rs ->
                new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                ), id
        );
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.queryForObject(sql, rs ->
                new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                ), account
        );
    }
}
