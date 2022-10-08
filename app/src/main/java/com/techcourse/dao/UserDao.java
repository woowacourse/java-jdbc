package com.techcourse.dao;

import com.techcourse.dao.statement.SelectResultSet;
import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
        });
    }

    public void update(final User user) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, user.getAccount());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setLong(4, user.getId());
        });
    }

    public List<User> findAll() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public User findById(final Long id) {
        String sql = "select * from users where id = ?";
        return (User) jdbcTemplate.executeQueryForObject(new SelectResultSet(), sql, new Object[]{id});
    }

    public User findByAccount(final String account) {
        String sql = "select * from users where account = ?";
        return (User) jdbcTemplate.executeQueryForObject(new SelectResultSet(), sql, new Object[]{account});
    }
}
