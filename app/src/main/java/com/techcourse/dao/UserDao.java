package com.techcourse.dao;

import com.interface21.jdbc.core.ArgumentPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) ->
            new User(rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"));
    private static final String UPDATE_USER_QUERY = """
            UPDATE users
            SET `account` = ?, `password` = ?, `email` = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public void insert(User user) {
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

    public void updateWithTransaction(User user) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = setUserArguments(user);

        jdbcTemplate.update(connection, UPDATE_USER_QUERY, argumentPreparedStatementSetter);
        log.info("user update successful");
    }

    public void update(User user) {
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = setUserArguments(user);

        jdbcTemplate.update(UPDATE_USER_QUERY, argumentPreparedStatementSetter);
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

    public Optional<User> findById(Long id) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        String sql = """
                SELECT id, account, password, email
                FROM users
                WHERE id =?
                """;

        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(id);

        Optional<User> user = Optional.ofNullable(jdbcTemplate.queryForObject(connection, sql, USER_ROW_MAPPER, argumentPreparedStatementSetter));
        log.info("user findById successful");
        return user;
    }

    public Optional<User> findByAccount(String account) {
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

    private ArgumentPreparedStatementSetter setUserArguments(User user) {
        return new ArgumentPreparedStatementSetter(
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                user.getId()
        );
    }
}
