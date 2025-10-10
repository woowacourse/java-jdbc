package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, email, password) values (?, ?, ?)";

        jdbcTemplate.executeUpdate(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
        });
    }

    public void update(final User user) {
        final String sql = "update users set account=?, email=?, password=? where id=?";

        jdbcTemplate.executeUpdate(sql, pstmt -> {
            pstmt.setString(1, user.getAccount());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        final var sql = "select id, account, email, password from users";
        return jdbcTemplate.executeSelectAll(sql, null, User.class);
    }

    public User findById(final Long id) {
        // 사용 시, 순서를 반드시 지켜야함
        final var sql = "select id, account, email, password from users where id = ?";
        Object[] params = {id};

        return jdbcTemplate.executeSelect(sql, params, User.class);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, email, password from users where account = ?";
        Object[] params = {account};
        return jdbcTemplate.executeSelect(sql, params, User.class);
    }
}
