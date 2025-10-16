package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("email"),
            rs.getString("password")
    );

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

        return jdbcTemplate.executeSelectAll(sql, userRowMapper);
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, email, password from users where id = ?";

        return jdbcTemplate.executeSelect(sql,pstmt -> pstmt.setLong(1, id), userRowMapper);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, email, password from users where account = ?";

        return jdbcTemplate.executeSelect(sql, pstmt -> pstmt.setString(1, account), userRowMapper);
    }
}
