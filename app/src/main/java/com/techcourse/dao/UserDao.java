package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(final User user) {
        final var sql = "update users set password = ?, email = ? where account = ?";
        jdbcTemplate.update(sql, user.getPassword(), user.getEmail(), user.getAccount());
    }

    public List<User> findAll() {
        final var sql = "select * from users";
        List<User> users = jdbcTemplate.query(sql, ((rs, rowNum) -> {
            Long id = rs.getLong("Id");
            String account = rs.getString("account");
            String password = rs.getString("password");
            String email = rs.getString("email");

            return new User(id, account, password, email);
        }));

        return users;
    }

    public Optional<User> findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Long foundId = rs.getLong("Id");
            String account = rs.getString("account");
            String password = rs.getString("password");
            String email = rs.getString("email");

            return new User(foundId, account, password, email);
        }, id);

        return Optional.of(user);
    }

    public Optional<User> findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        User user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Long id = rs.getLong("Id");
            String foundAccount = rs.getString("account");
            String password = rs.getString("password");
            String email = rs.getString("email");

            return new User(id, foundAccount, password, email);
        }, account);

        return Optional.of(user);
    }
}
