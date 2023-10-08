package com.techcourse.dao;

import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;


public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private final JdbcTemplate jdbcTemplate;

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final String sql = "update users set account = ?, password = ?, email = ? where users.id = ?";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select id, account, password, email from users";

        return jdbcTemplate.query(sql, rowMapper);
    }

    private final RowMapper<User> rowMapper = rs -> (
            new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            )
    );

    public Optional<User> findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    public List<User> findByAccount(final String account) {
        final String sql = "select id, account, password, email from users where users.account = ?";

        return jdbcTemplate.query(sql, rowMapper, account);
    }
}
