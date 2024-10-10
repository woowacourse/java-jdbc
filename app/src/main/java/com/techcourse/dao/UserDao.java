package com.techcourse.dao;

import com.interface21.jdbc.core.ArgumentPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) ->
            new User(rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"));

    private final JdbcTemplate jdbcTemplate;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        String sql = """
                INSERT INTO users (account, password, email)
                VALUES (?,?,?)
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(
                user.getAccount(),
                user.getPassword(),
                user.getEmail()
        );

        jdbcTemplate.update(sql, argumentPreparedStatementSetter);
        log.info("user insert successful");
    }

    public void update(final User user) {
        String sql = """
                UPDATE users
                SET `account` = ?, `password` = ?, `email` = ?
                WHERE id = ?
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );

        jdbcTemplate.update(sql, argumentPreparedStatementSetter);
        log.info("user update successful");
    }

    public List<User> findAll() {
        String sql = """
                SELECT id, account, password, email
                FROM users
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter();

        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, argumentPreparedStatementSetter);
        log.info("user findAll successful");
        return users;
    }

    public Optional<User> findById(final Long id) {
        String sql = """
                SELECT id, account, password, email
                FROM users
                WHERE id =?
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(id);

        Optional<User> user = Optional.ofNullable(jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, argumentPreparedStatementSetter));
        log.info("user findById successful");
        return user;
    }

    public Optional<User> findByAccount(final String account) {
        String sql = """
                SELECT id, account, password, email
                FROM users
                WHERE account = ?
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(account);

        Optional<User> user = Optional.ofNullable(jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, argumentPreparedStatementSetter));
        log.info("user findByAccount successful");
        return user;
    }
}
