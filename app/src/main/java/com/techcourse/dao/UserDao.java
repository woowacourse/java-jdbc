package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ObjectMapper;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper<User> objectMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    public UserDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        PreparedStatementSetter preparedStatementSetter = setParamToPreparedStatementSetter(
                user.getAccount(), user.getPassword(), user.getEmail());
        jdbcTemplate.update(sql, preparedStatementSetter);
        log.debug("query : {}", sql);
    }

    public void update(final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        PreparedStatementSetter preparedStatementSetter = setParamToPreparedStatementSetter(
                user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
        jdbcTemplate.update(sql, preparedStatementSetter);
        log.debug("query : {}", sql);
    }

    public void update(final Connection connection, final User user) {
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        PreparedStatementSetter preparedStatementSetter = setParamToPreparedStatementSetter(
                user.getAccount(),  user.getPassword(), user.getEmail(), user.getId());
        jdbcTemplate.update(connection, sql, preparedStatementSetter);
        log.debug("query : {}", sql);
    }

    public List<User> findAll() {
        final var sql = "select id, account, password, email from users";
        return jdbcTemplate.query(objectMapper, sql, pstmt -> {});
    }

    public User findById(final Long id) {
        final var sql = "select id, account, password, email from users where id = ?";
        return jdbcTemplate.queryForObject(objectMapper, sql, pstmt -> pstmt.setObject(1, id));
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";
        return jdbcTemplate.queryForObject(objectMapper, sql, pstmt -> pstmt.setObject(1, account));
    }

    private PreparedStatementSetter setParamToPreparedStatementSetter(Object... params) {
        return pstmt -> {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        };
    }
}


