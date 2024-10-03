package com.techcourse.dao;

import com.interface21.jdbc.core.GeneratedKeyHolder;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs -> {
        long id = rs.getLong("id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");

        return new User(id, account, password, email);
    });
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public User insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, keyHolder, user.getAccount(), user.getPassword(), user.getEmail());

        long id = (long) keyHolder.getKey();
        return new User(id, user);
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getAccount(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, account);
    }

    public void deleteAll() {
        final String sql = "delete from users";
        int update = jdbcTemplate.update(sql);
    }
}
