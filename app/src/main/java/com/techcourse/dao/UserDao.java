package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private static final RowMapper<User> mapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
        };

        jdbcTemplate.update(sql, setter);
    }

    public void update(final User user) {
        final var sql = "update users set account=?, password=?, email=? where id=?";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, user.getAccount());
            pstmt.setObject(2, user.getPassword());
            pstmt.setObject(3, user.getEmail());
            pstmt.setObject(4, user.getId());
        };

        jdbcTemplate.update(sql, setter);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        PreparedStatementSetter setter = pstmt -> {};

        return jdbcTemplate.query(sql, setter, mapper);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, id);
        };

        return jdbcTemplate.queryForObject(sql, setter, mapper);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, account);
        };

        return jdbcTemplate.queryForObject(sql, setter, mapper);
    }
}
